// Generated by CoffeeScript 1.12.4
(function() {
  var $, Attractor, Bounds, Formula, Grid, Params, Reactor, Renderer, Size, StandardCurve, accelerationGrid, attractor, canvas, centerPoint, colorTimeout, correctionCurve, createCustomMapper, ctx, emulatedFormula, hex2rgb, hue2rgb, input, pixelMapper, positionGrid, randomRange, randomizeAttractor, reactor, refreshingOperation, renderer, renderingEnabled, rgb2hsl, run, running, showState, size, toggle, updateBounds, updateBoundsSync, updateFormula, updateMapper, updateTTL, velocityGrid, verifyAttractor, viewBounds, viewCenterPoint, viewZoomLevel, zoomLevel;

  hue2rgb = function(p, q, t) {
    if (t < 0) {
      t += 1;
    }
    if (t > 1) {
      t -= 1;
    }
    if (t < 1 / 6) {
      return p + (q - p) * 6 * t;
    }
    if (t < 0.5) {
      return q;
    }
    if (t < 2 / 3) {
      return p + (q - p) * (2 / 3 - t) * 6;
    }
    return p;
  };

  rgb2hsl = function(ri, gi, bi) {
    var b, d, g, h, l, max, min, r, ref, s;
    ref = [ri / 255, gi / 255, bi / 255], r = ref[0], g = ref[1], b = ref[2];
    max = Math.max(r, g, b);
    min = Math.min(r, g, b);
    l = (max + min) / 2;
    if (max === min) {
      h = 0;
      s = 0;
    } else {
      d = max - min;
      s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
      h = (function() {
        switch (max) {
          case r:
            return (g - b) / d + (g < b ? 6 : 0);
          case g:
            return (b - r) / d + 2;
          case b:
            return (r - g) / d + 4;
        }
      })();
      h /= 6;
    }
    return {
      h: h,
      s: s,
      l: l
    };
  };

  hex2rgb = function(hex) {
    var matches;
    matches = /#?([0-9a-fA-F]{2})([0-9a-fA-F]{2})([0-9a-fA-F]{2})/.exec(hex);
    return {
      r: parseInt(matches[1], 16),
      g: parseInt(matches[2], 16),
      b: parseInt(matches[3], 16)
    };
  };

  Size = (function() {
    function Size(width, height) {
      this.width = width;
      this.height = height;
      this.area = this.width * this.height;
    }

    Size.prototype.contains = function(x, y) {
      return x >= 0 && x < this.width && y >= 0 && y < this.height;
    };

    return Size;

  })();

  Bounds = (function() {
    function Bounds(left, right, top, bottom) {
      this.left = left;
      this.right = right;
      this.top = top;
      this.bottom = bottom;
      this.width = this.right - this.left;
      this.height = this.bottom - this.top;
    }

    Bounds.prototype.contain = function(x, y) {
      return x >= this.left && x <= this.right && y >= this.top && y <= this.bottom;
    };

    return Bounds;

  })();

  Grid = (function() {
    function Grid(size1) {
      this.size = size1;
      this.view = new Float32Array(size.area);
      this.max = 0;
      this.min = 0;
      this.logDenominator = 0;
    }

    Grid.prototype.clear = function() {
      var i, j, ref;
      for (i = j = 0, ref = this.size.area; 0 <= ref ? j < ref : j > ref; i = 0 <= ref ? ++j : --j) {
        this.view[i] = 0;
      }
      this.max = 0;
      this.min = 0;
      this.logDenominator = 0;
      return this;
    };

    Grid.prototype.forEach = function(fn) {
      var i, j, ref;
      for (i = j = 0, ref = this.size.area; 0 <= ref ? j < ref : j > ref; i = 0 <= ref ? ++j : --j) {
        fn(this.view[i], i, this.view);
      }
      return this;
    };

    Grid.prototype.index = function(x, y) {
      return (y | 0) * this.size.width + (x | 0);
    };

    Grid.prototype.setIndex = function(index, value) {
      this.view[index] = value;
      if (value > this.max) {
        this.max = value;
        this.logDenominator = Math.log(this.max - this.min + 1);
      }
      if (value < this.min) {
        this.min = value;
        this.logDenominator = Math.log(this.max - this.min + 1);
      }
      return this;
    };

    Grid.prototype.addIndex = function(index, step) {
      if (step == null) {
        step = 1;
      }
      return this.setIndex(index, this.view[index] + step);
    };

    Grid.prototype.setXY = function(x, y, value) {
      if (!this.size.contains(x, y)) {
        return;
      }
      return this.setIndex(this.index(x, y), value);
    };

    Grid.prototype.addXY = function(x, y, step) {
      if (step == null) {
        step = 1;
      }
      if (!this.size.contains(x, y)) {
        return;
      }
      return this.addIndex(this.index(x, y), step);
    };

    return Grid;

  })();

  Renderer = (function() {
    function Renderer(size1, context) {
      var pixbuf;
      this.size = size1;
      this.context = context;
      this.imageData = this.context.getImageData(0, 0, this.size.width, this.size.height);
      pixbuf = new ArrayBuffer(this.imageData.data.length);
      this.pixelGrid = new Int32Array(pixbuf);
      this.pixel8 = new Uint8ClampedArray(pixbuf);
    }

    Renderer.prototype.render = function(pixelMapper) {
      var i, j, ref, value;
      for (i = j = 0, ref = this.size.area; 0 <= ref ? j < ref : j > ref; i = 0 <= ref ? ++j : --j) {
        value = pixelMapper(i);
        this.pixelGrid[i] = (255 << 24) | (value.b << 16) | (value.g << 8) | value.r;
      }
      this.imageData.data.set(this.pixel8);
      return this.context.putImageData(this.imageData, 0, 0);
    };

    return Renderer;

  })();

  this.GridMapper = {
    Zero: function() {
      return 0;
    },
    One: function() {
      return 1;
    },
    Constant: function(c) {
      return function() {
        return c;
      };
    },
    Binary: function(grid) {
      return function(index) {
        var ref;
        return (ref = grid.view[index] > 0) != null ? ref : {
          1: 0
        };
      };
    },
    Linear: function(grid) {
      return function(index) {
        return (grid.view[index] - grid.min) / (grid.max - grid.min);
      };
    },
    Logarithmic: function(grid) {
      return function(index) {
        if (grid.view[index] <= 0) {
          return 0;
        }
        return Math.log(grid.view[index] - grid.min + 1) / grid.logDenominator;
      };
    },
    Corrected: function(gridMapper, curve) {
      return function(index) {
        return curve(gridMapper(index));
      };
    }
  };

  this.PixelMapper = {
    Monochrome: function(gridMapper) {
      return function(index) {
        var value;
        value = gridMapper(index) * 255;
        return {
          r: value,
          g: value,
          b: value
        };
      };
    },
    Gradient: function(gradient, gridMapper) {
      return function(index) {
        return gradient(gridMapper(index));
      };
    },
    RGB: function(r, g, b) {
      return function(index) {
        return {
          r: r(index) * 255,
          g: g(index) * 255,
          b: b(index) * 255
        };
      };
    },
    HSL: function(h, s, l) {
      return function(index) {
        var p, q, value, vh, vl, vs;
        vh = h(index);
        vs = s(index);
        vl = l(index);
        if (vs === 0) {
          value = vl * 255;
          return {
            r: value,
            g: value,
            b: value
          };
        }
        q = vl < 0.5 ? vl * (1 + vs) : vl + vs - vl * vs;
        p = 2 * vl - q;
        return {
          r: hue2rgb(p, q, vh + 1 / 3) * 255,
          g: hue2rgb(p, q, vh) * 255,
          b: hue2rgb(p, q, vh - 1 / 3) * 255
        };
      };
    },
    Inverse: function(pixelMapper) {
      return function(index) {
        var result;
        result = pixelMapper(index);
        return {
          r: 255 - result.r,
          g: 255 - result.g,
          b: 255 - result.b
        };
      };
    }
  };

  StandardCurve = function(a, b, c) {
    return function(x) {
      var e, f, g, h, value;
      if (x < 0) {
        return 0;
      }
      if (x > 1) {
        return 1;
      }
      if (x <= 0.25) {
        e = 1.1428571428571429 - 38.857142857142857 * a + 27.428571428571429 * b - 6.857142857142857 * c;
        f = 0;
        g = -0.07142857142857143 + 6.428571428571429 * a - 1.7142857142857143 * b + 0.42857142857142857 * c;
        h = 0;
      } else if (x <= 0.5) {
        h = 0.10714285714285714 - 1.6428571428571429 * a + 1.5714285714285714 * b - 0.6428571428571429 * c;
        g = -1.3571428571428571 + 26.142857142857143 * a - 20.571428571428571 * b + 8.142857142857143 * c;
        f = 5.142857142857143 - 78.85714285714286 * a + 75.42857142857143 * b - 30.857142857142857 * c;
        e = -5.714285714285714 + 66.28571428571429 * a - 73.14285714285714 * b + 34.285714285714286 * c;
      } else if (x <= 0.75) {
        h = -3.3214285714285714 + 10.928571428571429 * a - 16.714285714285714 * b + 11.928571428571429 * c;
        g = 19.214285714285714 - 49.28571428571429 * a + 89.14285714285714 * b - 67.28571428571429 * c;
        f = -36 + 72 * a - 144 * b + 120 * c;
        e = 21.714285714285714 - 34.285714285714286 * a + 73.14285714285714 * b - 66.28571428571429 * c;
      } else {
        h = 13.071428571428571 - 6.428571428571429 * a + 25.714285714285714 * b - 32.428571428571429 * c;
        g = -46.357142857142857 + 20.142857142857143 * a - 80.57142857142857 * b + 110.14285714285714 * c;
        f = 51.42857142857143 - 20.571428571428571 * a + 82.28571428571429 * b - 116.57142857142857 * c;
        e = -17.142857142857143 + 6.857142857142857 * a - 27.428571428571429 * b + 38.857142857142857 * c;
      }
      value = h + x * (g + x * (f + x * e));
      if (value < 0) {
        return 0;
      }
      if (value > 1) {
        return 1;
      }
      return value;
    };
  };

  this.Gradient = {
    Cherry: function(value) {
      var scaled;
      if (value < 0.5) {
        scaled = value / 0.5;
        return {
          r: 116 * scaled,
          g: 4 * scaled,
          b: 28 * scaled
        };
      } else if (value < 0.85) {
        scaled = (value - 0.5) / 0.4;
        return {
          r: 116 + scaled * 32,
          g: 4 + scaled * 48,
          b: 28 + scaled * 88
        };
      } else {
        scaled = (value - 0.85) / 0.15;
        return {
          r: 148 + scaled * 107,
          g: 52 + scaled * 203,
          b: 116 + scaled * 139
        };
      }
    },
    Emerald: function(value) {
      var scaled;
      if (value < 0.4) {
        scaled = value / 0.4;
        return {
          r: 255 - scaled * 156,
          g: 255 - scaled * 77,
          b: 255 - scaled * 160
        };
      } else if (value < 0.51) {
        scaled = (value - 0.4) / 0.11;
        return {
          r: 99 - scaled * 27,
          g: 178 - scaled * 24,
          b: 95 - scaled * 41
        };
      } else if (value < 0.62) {
        scaled = (value - 0.51) / 0.11;
        return {
          r: 72 - scaled * 33,
          g: 154 - scaled * 44,
          b: 54 - scaled * 36
        };
      } else if (value < 0.85) {
        scaled = (value - 0.62) / 0.23;
        return {
          r: 39 - scaled * 30,
          g: 110 - scaled * 85,
          b: 18 - scaled * 4
        };
      } else {
        scaled = (value - 0.85) / 0.15;
        return {
          r: 9 - scaled * 9,
          g: 25 - scaled * 25,
          b: 14 - scaled * 14
        };
      }
    }
  };

  Formula = {
    SimpleBlended: function(params, x, y) {
      var ref;
      return {
        x: params[8] * (Math.sin(params[0] * y) + params[2] * Math.cos(params[0] * x)) + (1 - params[8]) * (y + params[4] * ((ref = x >= 0) != null ? ref : {
          1: -1
        }) * Math.sqrt(Math.abs(params[5] * x - params[6]))),
        y: params[8] * (Math.sin(params[1] * x) + params[3] * Math.cos(params[1] * y)) + (1 - params[8]) * (params[7] - x)
      };
    },
    Blended: function(params, x, y) {
      var ref;
      return {
        x: params[8] * (Math.sin(params[0] * y) + params[2] * Math.cos(params[0] * x)) + (1 - params[8]) * (y + params[4] * ((ref = x >= 0) != null ? ref : {
          1: -1
        }) * Math.sqrt(Math.abs(params[5] * x - params[6]))),
        y: params[9] * (Math.sin(params[1] * x) + params[3] * Math.cos(params[1] * y)) + (1 - params[9]) * (params[7] - x)
      };
    },
    Branched: function(params, x, y) {
      var nx, ny, ref;
      if (Math.random() < params[8]) {
        nx = Math.sin(params[0] * y) + params[2] * Math.cos(params[0] * x);
      } else {
        nx = y + params[4] * ((ref = x >= 0) != null ? ref : {
          1: -1
        }) * Math.sqrt(Math.abs(params[5] * x - params[6]));
      }
      if (Math.random() < params[8]) {
        ny = Math.sin(params[1] * x) + params[3] * Math.cos(params[1] * y);
      } else {
        ny = params[7] - x;
      }
      return {
        x: nx,
        y: ny
      };
    },
    SimpleBranched: function(params, x, y) {
      var nx, ny, ref;
      if (Math.random() < params[8]) {
        nx = Math.sin(params[0] * y) + params[2] * Math.cos(params[0] * x);
        ny = Math.sin(params[1] * x) + params[3] * Math.cos(params[1] * y);
      } else {
        nx = y + params[4] * ((ref = x >= 0) != null ? ref : {
          1: -1
        }) * Math.sqrt(Math.abs(params[5] * x - params[6]));
        ny = params[7] - x;
      }
      return {
        x: nx,
        y: ny
      };
    },
    Unnamed: function(params, x, y) {
      var ref;
      return {
        x: y + params[0] * ((ref = x >= 0) != null ? ref : {
          1: -1
        }) * Math.sqrt(Math.abs(params[1] * x - params[2])),
        y: params[3] - x
      };
    },
    Tinkerbell: function(params, x, y) {
      return {
        x: x * x - y * y + params[0] * x + params[1] * y,
        y: 2 * x * y + params[2] * x + params[3] * y
      };
    },
    DeJong: function(params, x, y) {
      return {
        x: Math.sin(params[0] * y) - Math.cos(params[1] * x),
        y: Math.sin(params[2] * x) - Math.cos(params[3] * y)
      };
    },
    GumowskiMira: function(params, x, y) {
      var nx, ny;
      nx = y + params[0] * (1 - params[2] * y * y) * y + params[1] * x + 2 * (1 - params[1]) * x * x / (1 + x * x);
      ny = -x + params[1] * nx + 2 * (1 - params[1]) * x * x / (1 + nx * nx);
      return {
        x: nx,
        y: ny
      };
    },
    Trigonometric: function(params, x, y) {
      return {
        x: params[0] * Math.sin(params[1] * y) + params[2] * Math.cos(params[3] * x),
        y: params[4] * Math.sin(params[5] * x) + params[6] * Math.cos(params[7] * y)
      };
    },
    DoubleTrigonometric: function(params, x, y) {
      return {
        x: params[0] * Math.sin(params[1] * y) + params[2] * Math.cos(params[3] * x) + params[4] * Math.sin(params[5] * x) + params[6] * Math.cos(params[7] * y),
        y: params[8] * Math.sin(params[9] * y) + params[10] * Math.cos(params[11] * x) + params[12] * Math.sin(params[13] * x) + params[14] * Math.cos(params[15] * y)
      };
    },
    Quadratic: function(params, x, y) {
      return {
        x: params[0] + (params[1] + params[2] * x + params[3] * y) * x + (params[4] + params[5] * y) * y,
        y: params[6] + (params[7] + params[8] * x + params[9] * y) * x + (params[10] + params[11] * y) * y
      };
    }
  };

  Formula.Tinkerbell.verify = true;

  Formula.Quadratic.verify = true;

  Params = {
    Standard: [2, 2, 2, 2, 2, 2, 2, 2, [0, 1], [0, 1]],
    GumowskiMira: [[0, 0.1], [-1, 0.5], [0, 0.1]],
    Sixteen: [2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2]
  };

  verifyAttractor = function(attractor) {
    var distance, dx, dy, ep, i, j, lyapunov, startdistance, v1, ve;
    lyapunov = 0;
    v1 = {
      x: Math.random() * 4 - 2,
      y: Math.random() * 4 - 2
    };
    ve = {
      x: v1.x + (Math.random() - 0.5) / 1000,
      y: v1.y + (Math.random() - 0.5) / 1000
    };
    dx = v1.x - ve.x;
    dy = v1.y - ve.y;
    startdistance = Math.sqrt(dx * dx + dy * dy);
    for (i = j = 1; j <= 5000; i = ++j) {
      v1 = attractor(v1.x, v1.y);
      if (i > 1000) {
        ep = attractor(ve.x, ve.y);
        dx = v1.x - ep.x;
        dy = v1.y - ep.y;
        distance = Math.sqrt(dx * dx + dy * dy);
        lyapunov += Math.log(Math.abs(distance / startdistance));
        ve.x = v1.x + startdistance * dx / distance;
        ve.y = v1.y + startdistance * dy / distance;
      }
    }
    return lyapunov >= 10;
  };

  randomRange = function(range) {
    var max, min, rnd;
    rnd = Math.random();
    if (typeof range === 'number') {
      return rnd * range * 2 - range;
    } else {
      min = range[0], max = range[1];
      return rnd * (max - min) + min;
    }
  };

  Attractor = function(formula, params) {
    var fn;
    fn = function(x, y) {
      return formula(params, x, y);
    };
    fn.params = params;
    return fn;
  };

  randomizeAttractor = function(formula, ranges) {
    var attractor, params;
    while (true) {
      params = ranges.map(randomRange);
      attractor = Attractor(formula, params);
      if (!(formula.verify === true && !verifyAttractor(attractor))) {
        break;
      }
    }
    return attractor;
  };

  Reactor = (function() {
    function Reactor(attractor1, options) {
      this.attractor = attractor1;
      if (options == null) {
        options = {};
      }
      this.bounds = options.bounds || new Bounds(-2, 2, -2, 2);
      this.count = options.count || 10000;
      this.ttl = options.ttl || 20;
      this.cache = null;
      this.onparticlemove = function(particle, reactor) {};
      this.reset();
    }

    Reactor.prototype.reset = function() {
      var i;
      return this.system = (function() {
        var j, ref, results;
        results = [];
        for (i = j = 0, ref = this.count; 0 <= ref ? j < ref : j > ref; i = 0 <= ref ? ++j : --j) {
          results.push({
            position: {
              x: Math.random() * this.bounds.width + this.bounds.left,
              y: Math.random() * this.bounds.height + this.bounds.top
            },
            velocity: {
              x: 0,
              y: 0
            },
            acceleration: {
              x: 0,
              y: 0
            },
            ttl: (Math.random() * this.ttl) | 0
          });
        }
        return results;
      }).call(this);
    };

    Reactor.prototype.step = function() {
      var i, j, len, particle, position, ref, result, results, velocity;
      ref = this.system;
      results = [];
      for (i = j = 0, len = ref.length; j < len; i = ++j) {
        particle = ref[i];
        if (particle.ttl <= 0) {
          particle = {
            position: {
              x: Math.random() * this.bounds.width + this.bounds.left,
              y: Math.random() * this.bounds.height + this.bounds.top
            },
            velocity: {
              x: 0,
              y: 0
            },
            acceleration: {
              x: 0,
              y: 0
            },
            ttl: this.ttl
          };
        }
        position = this.attractor(particle.position.x, particle.position.y);
        velocity = {
          x: position.x - particle.position.x,
          y: position.y - particle.position.y
        };
        result = {
          position: position,
          velocity: velocity,
          acceleration: {
            x: velocity.x - particle.velocity.x,
            y: velocity.y - particle.velocity.y
          },
          ttl: particle.ttl - 1
        };
        this.onparticlemove(result, this);
        results.push(this.system[i] = result);
      }
      return results;
    };

    return Reactor;

  })();

  $ = function(id) {
    return document.getElementById(id);
  };

  canvas = $('Canvas');

  ctx = canvas.getContext('2d');

  size = new Size(canvas.width | 0, canvas.height | 0);

  positionGrid = new Grid(size);

  velocityGrid = new Grid(size);

  accelerationGrid = new Grid(size);

  renderer = new Renderer(size, ctx);

  input = {
    "formula": "Unnamed",
    "params": [0.4355084184519855, -0.23559170975080246, -1.3620484834403603, -0.6547066518285254, 0.5852320106598246, 0.3428832489223792, -1.9372635523978978, 1.3320091601999255, 0.5626223396408057, 0.7998837324627617],
    "ttl": 500,
    "bounds": {
      "left": -2,
      "right": 2,
      "top": -2,
      "bottom": 2,
      "width": 4,
      "height": 4
    },
    "viewBounds": {
      "left": -2.3,
      "right": 1.7,
      "top": -2.4,
      "bottom": 1.6,
      "width": 4,
      "height": 4
    },
    "correction": {
      "enabled": false,
      "a": 0.25,
      "b": 0.5,
      "c": 0.75
    }
  };

  attractor = Attractor(Formula[input.formula], input.params);

  emulatedFormula = function(_params, x, y) {
    var a, b, weight;
    weight = 0.13299989025108516;
    a = Formula.Trigonometric([1, -1.0642758188769219, -0.15641715284436941, -1.0642758188769219, 1, -1.3800999578088522, 1.3192101996392012, -1.3800999578088522], x, y);
    b = Formula.Unnamed([1.88299339171499, -0.13897148426622152, -0.150858367793262, 1.5277614956721663], x, y);
    return {
      x: a.x * weight + b.x * 0.86700010974891484,
      y: a.y * weight + b.y * 0.86700010974891484
    };
  };

  zoomLevel = 2.0;

  viewZoomLevel = 2.0;

  centerPoint = {
    x: 0,
    y: 0
  };

  viewCenterPoint = {
    x: 0,
    y: 0
  };

  viewBounds = new Bounds(-viewZoomLevel + viewCenterPoint.x, viewZoomLevel + viewCenterPoint.x, -viewZoomLevel + viewCenterPoint.y, viewZoomLevel + viewCenterPoint.y);

  reactor = new Reactor(attractor, {
    count: 50000
  });

  reactor.onparticlemove = function(particle, reactor) {
    var accel, pos, vel, x, y;
    pos = particle.position;
    vel = particle.velocity;
    accel = particle.acceleration;
    x = (pos.x - viewBounds.left) / viewBounds.width * positionGrid.size.width;
    y = (pos.y - viewBounds.top) / viewBounds.height * positionGrid.size.height;
    positionGrid.addXY(x | 0, y | 0);
    velocityGrid.addXY(x | 0, y | 0, Math.sqrt(vel.x * vel.x + vel.y * vel.y));
    return accelerationGrid.addXY(x | 0, y | 0, Math.sqrt(accel.x * accel.x + accel.y * accel.y));
  };

  correctionCurve = StandardCurve(0.25, 0.5, 0.75);

  this.GridModifier = {
    None: function(gridMapper) {
      return gridMapper;
    },
    Corrected: function(correctionCurve) {
      return function(gridMapper) {
        return GridMapper.Corrected(gridMapper, correctionCurve);
      };
    },
    Inverted: function(gridMapper) {
      return function(index) {
        return 1 - gridMapper(index);
      };
    },
    Multiplied: function(constant, gridMapper) {
      return function(index) {
        return constant * gridMapper(index);
      };
    },
    Added: function(constant, gridMapper) {
      return function(index) {
        return constant + gridMapper(index);
      };
    },
    Merged: function(gridMapperA, gridMapperB) {
      return function(index) {
        return gridMapperA(index) + gridMapperB(index);
      };
    }
  };

  this.Presets = {
    Binary: function() {
      return PixelMapper.Monochrome(GridMapper.Binary(positionGrid));
    },
    Monochrome: function(gridModifier) {
      return PixelMapper.Monochrome(gridModifier(GridMapper.Logarithmic(positionGrid)));
    },
    PositionLinear: function(gridModifier) {
      return PixelMapper.Monochrome(gridModifier(GridMapper.Linear(velocityGrid)));
    },
    Velocity: function(gridModifier) {
      return PixelMapper.Monochrome(gridModifier(GridMapper.Logarithmic(velocityGrid)));
    },
    Acceleration: function(gridModifier) {
      return PixelMapper.Monochrome(gridModifier(GridMapper.Logarithmic(accelerationGrid)));
    },
    PVA: function(gridModifier) {
      var modLog;
      modLog = function(grid) {
        return gridModifier(GridMapper.Logarithmic(grid));
      };
      return PixelMapper.RGB(modLog(positionGrid), modLog(velocityGrid), modLog(accelerationGrid));
    },
    APV: function(gridModifier) {
      var modLog;
      modLog = function(grid) {
        return gridModifier(GridMapper.Logarithmic(grid));
      };
      return PixelMapper.RGB(modLog(accelerationGrid), modLog(positionGrid), modLog(velocityGrid));
    },
    VAP: function(gridModifier) {
      var modLog;
      modLog = function(grid) {
        return gridModifier(GridMapper.Logarithmic(grid));
      };
      return PixelMapper.RGB(modLog(velocityGrid), modLog(accelerationGrid), modLog(positionGrid));
    },
    Classic: function(gridModifier) {
      var h, l, s;
      h = GridModifier.Multiplied(0.2, gridModifier(GridMapper.Logarithmic(velocityGrid)));
      s = GridModifier.Added(0.6, GridModifier.Multiplied(0.4, GridMapper.Linear(accelerationGrid)));
      l = gridModifier(GridMapper.Logarithmic(positionGrid));
      return PixelMapper.HSL(h, s, l);
    },
    DeepRed: function(gridModifier) {
      var h, l, s;
      h = GridModifier.Added(-0.125, GridModifier.Multiplied(0.2, GridMapper.Logarithmic(accelerationGrid)));
      s = GridModifier.Multiplied(0.8, GridModifier.Inverted(GridMapper.Linear(velocityGrid)));
      l = GridModifier.Multiplied(0.8, gridModifier(GridMapper.Logarithmic(positionGrid)));
      return PixelMapper.HSL(h, s, l);
    },
    IceBlue: function(gridModifier) {
      var h, l, s;
      h = GridModifier.Added(0.6, GridModifier.Multiplied(0.15, GridMapper.Logarithmic(accelerationGrid)));
      s = GridModifier.Multiplied(0.4, GridModifier.Inverted(GridMapper.Linear(velocityGrid)));
      l = gridModifier(GridMapper.Logarithmic(positionGrid));
      return PixelMapper.HSL(h, s, l);
    },
    Emerald: function(gridModifier) {
      return PixelMapper.Gradient(Gradient.Emerald, gridModifier(GridMapper.Logarithmic(positionGrid)));
    },
    Cherry: function(gridModifier) {
      return PixelMapper.Gradient(Gradient.Cherry, gridModifier(GridMapper.Logarithmic(positionGrid)));
    },
    Testing: function(gridModifier) {
      var h, l, s;
      h = GridModifier.Added(-0.35, GridModifier.Multiplied(0.45, gridModifier(GridMapper.Logarithmic(velocityGrid))));
      s = GridModifier.Added(0.6, GridModifier.Multiplied(0.4, GridMapper.Linear(accelerationGrid)));
      l = gridModifier(GridMapper.Logarithmic(positionGrid));
      return PixelMapper.HSL(h, s, l);
    }
  };

  pixelMapper = Presets.Monochrome(GridModifier.None);

  running = false;

  renderingEnabled = true;

  run = function() {
    reactor.step();
    if (renderingEnabled) {
      renderer.render(pixelMapper);
    }
    if (running) {
      return requestAnimationFrame(run);
    }
  };

  toggle = $('Toggle');

  this.setRunning = function(r) {
    running = r;
    toggle.innerText = running ? 'Stop' : 'Start';
    if (running) {
      return requestAnimationFrame(run);
    }
  };

  toggle.onclick = function() {
    return setRunning(!running);
  };

  setRunning(window.location.search !== '?stop');

  $('Step').onclick = function() {
    running = false;
    return run();
  };

  $('Save').onclick = function() {
    return window.open(canvas.toDataURL('image/png'));
  };

  refreshingOperation = function(fn) {
    fn();
    reactor.reset();
    positionGrid.clear();
    velocityGrid.clear();
    accelerationGrid.clear();
    if (renderingEnabled && !running) {
      return renderer.render(pixelMapper);
    }
  };

  showState = function() {
    return $('State').innerText = JSON.stringify({
      formula: $('Formula').value,
      params: reactor.attractor.params,
      ttl: $('TTL').valueAsNumber,
      bounds: reactor.bounds,
      viewBounds: viewBounds,
      correction: {
        enabled: $('Correction').checked,
        a: $('CorrectionA').value * 0.01,
        b: $('CorrectionB').value * 0.01,
        c: $('CorrectionC').value * 0.01
      }
    });
  };

  updateFormula = function() {
    var formulaName, paramsName;
    formulaName = $('Formula').value;
    paramsName = (function() {
      switch (formulaName) {
        case 'DoubleTrigonometric':
          return 'Sixteen';
        case 'Quadratic':
          return 'Sixteen';
        case 'GumowskiMira':
          return 'GumowskiMira';
        default:
          return 'Standard';
      }
    })();
    refreshingOperation(function() {
      return reactor.attractor = randomizeAttractor(Formula[formulaName], Params[paramsName]);
    });
    return showState();
  };

  updateMapper = function() {
    var a, b, c, preset, presetValue;
    presetValue = $('Preset').value;
    preset = presetValue === 'Custom' ? ($('CustomControls').className = "controls", createCustomMapper()) : ($('CustomControls').className = "controls hidden", Presets[presetValue]);
    if ($('Correction').checked) {
      a = $('CorrectionA').value * 0.01;
      b = $('CorrectionB').value * 0.01;
      c = $('CorrectionC').value * 0.01;
      correctionCurve = StandardCurve(a, b, c);
      pixelMapper = preset(GridModifier.Corrected(correctionCurve));
    } else {
      pixelMapper = preset(GridModifier.None);
    }
    if ($('Inverted').checked) {
      pixelMapper = PixelMapper.Inverse(pixelMapper);
    }
    if (renderingEnabled && !running) {
      renderer.render(pixelMapper);
    }
    return showState();
  };

  createCustomMapper = function() {
    var color, hsl, shift;
    color = hex2rgb($('Color').value);
    hsl = rgb2hsl(color.r, color.g, color.b);
    shift = $('ColorShift').valueAsNumber / 360;
    return function(gridModifier) {
      var h, l, s;
      h = GridModifier.Added(hsl.h, GridModifier.Multiplied(shift, GridMapper.Logarithmic(velocityGrid)));
      s = GridMapper.Constant(hsl.s);
      l = GridModifier.Multiplied(hsl.l, gridModifier(GridMapper.Logarithmic(positionGrid)));
      return PixelMapper.HSL(h, s, l);
    };
  };

  updateBounds = function() {
    viewZoomLevel = Math.pow(2, 5 - $('ViewZoom').valueAsNumber * 0.5);
    viewCenterPoint = {
      x: $('ViewX').valueAsNumber,
      y: $('ViewY').valueAsNumber
    };
    if ($('SyncBounds').checked) {
      zoomLevel = viewZoomLevel;
      centerPoint = viewCenterPoint;
    } else {
      zoomLevel = Math.pow(2, 5 - $('Zoom').valueAsNumber * 0.5);
      centerPoint = {
        x: $('CenterX').valueAsNumber,
        y: $('CenterY').valueAsNumber
      };
    }
    refreshingOperation(function() {
      reactor.bounds = new Bounds(-zoomLevel + centerPoint.x, zoomLevel + centerPoint.x, -zoomLevel + centerPoint.y, zoomLevel + centerPoint.y);
      return viewBounds = new Bounds(-viewZoomLevel + viewCenterPoint.x, viewZoomLevel + viewCenterPoint.x, -viewZoomLevel + viewCenterPoint.y, viewZoomLevel + viewCenterPoint.y);
    });
    return showState();
  };

  updateBoundsSync = function() {
    var sync;
    sync = $('SyncBounds').checked;
    $('Zoom').disabled = sync;
    $('CenterX').disabled = sync;
    $('CenterY').disabled = sync;
    return updateBounds();
  };

  updateTTL = function() {
    var ttl;
    ttl = $('TTL').valueAsNumber;
    refreshingOperation(function() {
      return reactor.ttl = ttl;
    });
    return showState();
  };

  $('Rendering').onchange = function() {
    renderingEnabled = $('Rendering').checked;
    if (renderingEnabled && !running) {
      return renderer.render(pixelMapper);
    }
  };

  $('Formula').onchange = updateFormula;

  $('Randomize').onclick = updateFormula;

  $('Preset').onchange = updateMapper;

  $('Inverted').onchange = updateMapper;

  $('Correction').onchange = updateMapper;

  $('CorrectionA').onchange = updateMapper;

  $('CorrectionB').onchange = updateMapper;

  $('CorrectionC').onchange = updateMapper;

  $('Zoom').onchange = updateBounds;

  $('CenterX').onchange = updateBounds;

  $('CenterY').onchange = updateBounds;

  $('ViewZoom').onchange = updateBounds;

  $('ViewX').onchange = updateBounds;

  $('ViewY').onchange = updateBounds;

  $('SyncBounds').onchange = updateBoundsSync;

  colorTimeout = null;

  $('Color').onchange = function() {
    clearTimeout(colorTimeout);
    return colorTimeout = setTimeout(updateMapper, 150);
  };

  $('ColorShiftSlider').onchange = function() {
    $('ColorShift').value = $('ColorShiftSlider').value;
    return updateMapper();
  };

  $('ColorShift').onchange = function() {
    $('ColorShiftSlider').value = $('ColorShift').value;
    return updateMapper();
  };

  $('TTLSlider').onchange = function() {
    $('TTL').value = $('TTLSlider').value;
    return updateTTL();
  };

  $('TTL').onchange = function() {
    $('TTLSlider').value = $('TTL').value;
    return updateTTL();
  };

  $('ResetBounds').onclick = function(e) {
    e.preventDefault();
    $('ViewZoom').value = 8;
    $('ViewX').value = 0;
    $('ViewY').value = 0;
    return updateBounds();
  };

  $('ResetCorrection').onclick = function(e) {
    e.preventDefault();
    $('CorrectionA').value = 25;
    $('CorrectionB').value = 50;
    $('CorrectionC').value = 75;
    return updateMapper();
  };

  showState();

}).call(this);

//# sourceMappingURL=pierdut.js.map