hue2rgb = (p, q, t) ->
  t += 1 if t < 0
  t -= 1 if t > 1

  return p + (q - p) * 6 * t         if t < 1/6
  return q                           if t < 0.5
  return p + (q - p) * (2/3 - t) * 6 if t < 2/3

  return p

rgb2hsl = (ri, gi, bi) ->
  [r, g, b] = [ri / 255, gi / 255, bi / 255]
  max = Math.max(r, g, b)
  min = Math.min(r, g, b)

  l = (max + min) / 2

  if (max == min)
    h = 0
    s = 0
  else
    d = max - min
    s = if l > 0.5 then d / (2 - max - min) else d / (max + min)

    h = switch max
      when r then (g - b) / d + (if g < b then 6 else 0)
      when g then (b - r) / d + 2
      when b then (r - g) / d + 4

    h /= 6

  h: h
  s: s
  l: l

hex2rgb = (hex) ->
  matches = /#?([0-9a-fA-F]{2})([0-9a-fA-F]{2})([0-9a-fA-F]{2})/.exec(hex)

  r: parseInt(matches[1], 16)
  g: parseInt(matches[2], 16)
  b: parseInt(matches[3], 16)

roughlyEquals = (a, b) ->
  Math.abs(a - b) < 0.0001

class Size
  constructor: (@width, @height) ->
    @area = @width * @height

  contains: (x, y) ->
    x >= 0 && x < @width && y >= 0 && y < @height

class Bounds
  constructor: (@left, @right, @top, @bottom) ->
    @width = @right - @left
    @height = @bottom - @top

  contain: (x, y) ->
    x >= @left && x <= @right && y >= @top && y <= @bottom

  @fromZoomAndCenter: (zoomLevel, centerX, centerY) ->
    zoom = Math.pow(2, 5 - zoomLevel * 0.5)
    new Bounds(-zoom + centerX, zoom + centerX, -zoom + centerY, zoom + centerY)

  @areValid: (bounds) ->
    bounds != null \
    && (['left', 'right', 'top', 'bottom'].every (key) -> typeof bounds[key] == 'number') \
    && bounds.left <= bounds.right && bounds.top <= bounds.bottom

class Grid
  constructor: (@size) ->
    @view = new Float32Array(size.area)

    @max = 0
    @min = 0
    @logDenominator = 0

  clear: ->
    for i in [0...@size.area]
      @view[i] = 0

    @max = 0
    @min = 0
    @logDenominator = 0

    return @

  forEach: (fn) ->
    for i in [0...@size.area]
      fn(@view[i], i, @view)

    return @

  index: (x, y) -> (y|0) * @size.width + (x|0)

  setIndex: (index, value) ->
    @view[index] = value

    if value > @max
      @max = value
      @logDenominator = Math.log(@max - @min + 1)
    if value < @min
      @min = value
      @logDenominator = Math.log(@max - @min + 1)

    return @

  addIndex: (index, step = 1) -> @setIndex(index, @view[index] + step)

  setXY: (x, y, value) ->
    return unless @size.contains(x, y)
    @setIndex(@index(x, y), value)
  addXY: (x, y, step = 1) ->
    return unless @size.contains(x, y)
    @addIndex(@index(x, y), step)

class Renderer
  constructor: (@size, @context) ->
    @imageData = @context.getImageData(0, 0, @size.width, @size.height)

    pixbuf = new ArrayBuffer(@imageData.data.length)
    @pixelGrid = new Int32Array(pixbuf)
    @pixel8 = new Uint8ClampedArray(pixbuf)

  render: (pixelMapper) ->
    for i in [0...@size.area]
      value = pixelMapper(i)

      @pixelGrid[i] = (255 << 24) | (value.b << 16) | (value.g << 8) | value.r

    @imageData.data.set(@pixel8)
    @context.putImageData(@imageData, 0, 0)

@GridMapper =
  Zero: -> 0
  One: -> 1
  Constant: (c) ->
    return -> c
  Binary: (grid) ->
    return (index) -> (grid.view[index] > 0) ? 1 : 0
  Linear: (grid) ->
    return (index) -> (grid.view[index] - grid.min) / (grid.max - grid.min)
  Logarithmic: (grid) ->
    return (index) ->
      return 0 if grid.view[index] <= 0
      Math.log(grid.view[index] - grid.min + 1) / grid.logDenominator
  Corrected: (gridMapper, curve) ->
    return (index) ->
      curve(gridMapper(index))

@PixelMapper =
  Monochrome: (gridMapper) ->
    return (index) ->
      value = gridMapper(index) * 255

      r: value
      g: value
      b: value
  Gradient: (gradient, gridMapper) ->
    return (index) ->
      gradient(gridMapper(index))
  RGB: (r, g, b) ->
    return (index) ->
      r: r(index) * 255
      g: g(index) * 255
      b: b(index) * 255
  HSL: (h, s, l) ->
    return (index) ->
      vh = h(index)
      vs = s(index)
      vl = l(index)

      if vs == 0
        value = vl * 255
        return {
          r: value
          g: value
          b: value
        }

      q = if vl < 0.5 then vl * (1 + vs) else vl + vs - vl * vs
      p = 2 * vl - q

      r: hue2rgb(p, q, vh + 1/3) * 255
      g: hue2rgb(p, q, vh) * 255
      b: hue2rgb(p, q, vh - 1/3) * 255
  Inverse: (pixelMapper) ->
    return (index) ->
      result = pixelMapper(index)

      r: 255 - result.r
      g: 255 - result.g
      b: 255 - result.b

StandardCurve = (a, b, c) ->
  return (x) ->
    return 0 if x < 0
    return 1 if x > 1

    if x <= 0.25
      e = 1.1428571428571429 - 38.857142857142857 * a + 27.428571428571429 * b - 6.857142857142857 * c
      f = 0
      g = -0.07142857142857143 + 6.428571428571429 * a - 1.7142857142857143 * b + 0.42857142857142857 * c
      h = 0
    else if x <= 0.5
      h = 0.10714285714285714 - 1.6428571428571429 * a + 1.5714285714285714 * b - 0.6428571428571429 * c
      g = -1.3571428571428571 + 26.142857142857143 * a - 20.571428571428571 * b + 8.142857142857143 * c
      f = 5.142857142857143 - 78.85714285714286 * a + 75.42857142857143 * b - 30.857142857142857 * c
      e = -5.714285714285714 + 66.28571428571429 * a - 73.14285714285714 * b + 34.285714285714286 * c
    else if x <= 0.75
      h = -3.3214285714285714 + 10.928571428571429 * a - 16.714285714285714 * b + 11.928571428571429 * c
      g = 19.214285714285714 - 49.28571428571429 * a + 89.14285714285714 * b - 67.28571428571429 * c
      f = -36 + 72 * a - 144 * b + 120 * c
      e = 21.714285714285714 - 34.285714285714286 * a + 73.14285714285714 * b - 66.28571428571429 * c
    else
      h = 13.071428571428571 - 6.428571428571429 * a + 25.714285714285714 * b - 32.428571428571429 * c
      g = -46.357142857142857 + 20.142857142857143 * a - 80.57142857142857 * b + 110.14285714285714 * c
      f = 51.42857142857143 - 20.571428571428571 * a + 82.28571428571429 * b - 116.57142857142857 * c
      e = -17.142857142857143 + 6.857142857142857 * a - 27.428571428571429 * b + 38.857142857142857 * c

    # ex^3 + fx^2 + gx + h
    value = h + x * (g + x * (f + x * e))
    return 0 if value < 0
    return 1 if value > 1
    value

@Gradient =
  Cherry: (value) ->
    if value < 0.5
      scaled = value / 0.5

      r: 116 * scaled
      g: 4 * scaled
      b: 28 * scaled
    else if value < 0.85
      scaled = (value - 0.5) / 0.4

      r: 116 + scaled * 32 # 148
      g: 4 + scaled * 48   # 52
      b: 28 + scaled * 88  # 116
    else
      scaled = (value - 0.85) / 0.15

      r: 148 + scaled * 107 # 255
      g: 52 + scaled * 203  # 255
      b: 116 + scaled * 139 # 255
  Emerald: (value) ->
    if value < 0.4
      scaled = value / 0.4

      r: 255 - scaled * 156 # 99
      g: 255 - scaled * 77  # 178
      b: 255 - scaled * 160 # 95
    else if value < 0.51
      scaled = (value - 0.4) / 0.11

      r: 99 - scaled * 27  # 72
      g: 178 - scaled * 24 # 154
      b: 95 - scaled * 41  # 54
    else if value < 0.62
      scaled = (value - 0.51) / 0.11

      r: 72 - scaled * 33  # 39
      g: 154 - scaled * 44 # 110
      b: 54 - scaled * 36  # 18
    else if value < 0.85
      scaled = (value - 0.62) / 0.23

      r: 39 - scaled * 30  # 9
      g: 110 - scaled * 85 # 25
      b: 18 - scaled * 4   # 14
    else
      scaled = (value - 0.85) / 0.15

      r: 9 - scaled * 9
      g: 25 - scaled * 25
      b: 14 - scaled * 14

#########################################################################################

Formula =
  SimpleBlended: (params, x, y) -> # formerly known as Blut
    x: params[8] * (Math.sin(params[0] * y) + params[2] * Math.cos(params[0] * x)) + (1 - params[8]) * (y + params[4] * (x >= 0 ? 1 : -1) * Math.sqrt(Math.abs(params[5] * x - params[6]))),
    y: params[8] * (Math.sin(params[1] * x) + params[3] * Math.cos(params[1] * y)) + (1 - params[8]) * (params[7] - x)
  Blended: (params, x, y) ->
    x: params[8] * (Math.sin(params[0] * y) + params[2] * Math.cos(params[0] * x)) + (1 - params[8]) * (y + params[4] * (x >= 0 ? 1 : -1) * Math.sqrt(Math.abs(params[5] * x - params[6])))
    y: params[9] * (Math.sin(params[1] * x) + params[3] * Math.cos(params[1] * y)) + (1 - params[9]) * (params[7] - x)
  Branched: (params, x, y) ->
    if Math.random() < params[8]
      nx = Math.sin(params[0] * y) + params[2] * Math.cos(params[0] * x)
    else
      nx = y + params[4] * (x >= 0 ? 1 : -1) * Math.sqrt(Math.abs(params[5] * x - params[6]))

    if Math.random() < params[8]
      ny = Math.sin(params[1] * x) + params[3] * Math.cos(params[1] * y)
    else
      ny = params[7] - x

    x: nx
    y: ny
  SimpleBranched: (params, x, y) ->
    if Math.random() < params[8]
      nx = Math.sin(params[0] * y) + params[2] * Math.cos(params[0] * x)
      ny = Math.sin(params[1] * x) + params[3] * Math.cos(params[1] * y)
    else
      nx = y + params[4] * (x >= 0 ? 1 : -1) * Math.sqrt(Math.abs(params[5] * x - params[6]))
      ny = params[7] - x

    x: nx
    y: ny
  Unnamed: (params, x, y) ->
    x: y + params[0] * (x >= 0 ? 1 : -1) * Math.sqrt(Math.abs(params[1] * x - params[2]))
    y: params[3] - x
  Tinkerbell: (params, x, y) ->
    x: x * x - y * y + params[0] * x + params[1] * y
    y: 2 * x * y + params[2] * x + params[3] * y
  DeJong: (params, x, y) ->
    x: Math.sin(params[0] * y) - Math.cos(params[1] * x)
    y: Math.sin(params[2] * x) - Math.cos(params[3] * y)
  GumowskiMira: (params, x, y) ->
    nx = y + params[0] * (1 - params[2] * y * y) * y + params[1] * x + 2 * (1 - params[1]) * x * x / (1 + x * x)
    ny = -x + params[1] * nx + 2 * (1 - params[1]) * x * x / (1 + nx * nx)

    x: nx
    y: ny
  Trigonometric: (params, x, y) ->
    x: params[0] * Math.sin(params[1] * y) + params[2] * Math.cos(params[3] * x)
    y: params[4] * Math.sin(params[5] * x) + params[6] * Math.cos(params[7] * y)
  DoubleTrigonometric: (params, x, y) ->
    x: params[0] * Math.sin(params[1] * y) + params[2] * Math.cos(params[3] * x) + params[4] * Math.sin(params[5] * x) + params[6] * Math.cos(params[7] * y)
    y: params[8] * Math.sin(params[9] * y) + params[10] * Math.cos(params[11] * x) + params[12] * Math.sin(params[13] * x) + params[14] * Math.cos(params[15] * y)
  Quadratic: (params, x, y) ->
    x: params[0] + (params[1] + params[2] * x + params[3] * y) * x + (params[4] + params[5] * y) * y;
    y: params[6] + (params[7] + params[8] * x + params[9] * y) * x + (params[10] + params[11] * y) * y;

Formula.Tinkerbell.verify = true
Formula.Quadratic.verify = true

Params =
  Standard: [2, 2, 2, 2, 2, 2, 2, 2, [0,1], [0,1]]
  GumowskiMira: [[0, 0.1], [-1, 0.5], [0, 0.1]]
  Sixteen: [2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2]

verifyAttractor = (attractor) ->
  lyapunov = 0

  v1 =
    x: Math.random() * 4 - 2
    y: Math.random() * 4 - 2
  ve =
    x: v1.x + (Math.random() - 0.5) / 1000
    y: v1.y + (Math.random() - 0.5) / 1000

  dx = v1.x - ve.x
  dy = v1.y - ve.y

  startdistance = Math.sqrt(dx * dx + dy * dy)

  for i in [1..5000]
    v1 = attractor(v1.x, v1.y)

    if i > 1000
      ep = attractor(ve.x, ve.y)

      dx = v1.x - ep.x
      dy = v1.y - ep.y

      distance = Math.sqrt(dx * dx + dy * dy)

      lyapunov += Math.log(Math.abs(distance / startdistance))

      ve.x = v1.x + startdistance * dx / distance
      ve.y = v1.y + startdistance * dy / distance

  return lyapunov >= 10

randomRange = (range) ->
  rnd = Math.random()

  if typeof range == 'number'
    rnd * range * 2 - range
  else
    [min, max] = range
    rnd * (max - min) + min

Attractor = (formula, params) ->
  fn = (x, y) -> formula(params, x, y)
  fn.params = params
  return fn

randomizeAttractor = (formula, ranges) ->
  loop
    params = ranges.map randomRange
    attractor = Attractor(formula, params)

    break unless formula.verify == true and not verifyAttractor(attractor)
  attractor

class Reactor
  constructor: (@attractor, options = {}) ->
    @bounds = options.bounds || new Bounds(-2, 2, -2, 2)
    @count = options.count || 10000
    @ttl = options.ttl || 50
    @cache = null

    @onparticlemove = (particle, reactor) ->

    @reset()

  reset: ->
    @system = for i in [0...@count]
      position:
        x: Math.random() * @bounds.width + @bounds.left
        y: Math.random() * @bounds.height + @bounds.top
      velocity:
        x: 0
        y: 0
      acceleration:
        x: 0
        y: 0
      ttl: (Math.random() * @ttl) | 0

  step: ->
    for particle, i in @system
      if particle.ttl <= 0
        particle = {
          position:
            x: Math.random() * @bounds.width + @bounds.left
            y: Math.random() * @bounds.height + @bounds.top
          velocity:
            x: 0
            y: 0
          acceleration:
            x: 0
            y: 0
          ttl: @ttl
        }

      position = @attractor(particle.position.x, particle.position.y)
      velocity = {
        x: position.x - particle.position.x
        y: position.y - particle.position.y
      }
      result = {
        position: position
        velocity: velocity
        acceleration:
          x: velocity.x - particle.velocity.x
          y: velocity.y - particle.velocity.y
        ttl: particle.ttl - 1
      }

      @onparticlemove(result, @)
      @system[i] = result

#########################################################################################

$ = (id) -> document.getElementById(id)

canvas = $('Canvas')
ctx = canvas.getContext('2d')
size = new Size(canvas.width|0, canvas.height|0)

positionGrid = new Grid(size)
velocityGrid = new Grid(size)
accelerationGrid = new Grid(size)
renderer = new Renderer(size, ctx)

# input = {"formula":"Unnamed","params":[0.4355084184519855,-0.23559170975080246,-1.3620484834403603,-0.6547066518285254,0.5852320106598246,0.3428832489223792,-1.9372635523978978,1.3320091601999255,0.5626223396408057,0.7998837324627617],"ttl":500,"bounds":{"left":-2,"right":2,"top":-2,"bottom":2,"width":4,"height":4},"viewBounds":{"left":-2.3,"right":1.7,"top":-2.4,"bottom":1.6,"width":4,"height":4},"correction":{"enabled":false,"a":0.25,"b":0.5,"c":0.75}}
# input = {"formula":"DeJong","params":[1.6623940085992217,-0.6880100890994072,1.4784153904765844,1.7967103328555822,1.3117856830358505,-1.7860524505376816,0.037012672051787376,0.9399532228708267,0.9259882022161037,0.6146395546384156],"ttl":20,"bounds":{"left":-2,"right":2,"top":-2,"bottom":2,"width":4,"height":4},"viewBounds":{"left":-2,"right":2,"top":-2,"bottom":2,"width":4,"height":4},"correction":{"enabled":false,"a":0.25,"b":0.5,"c":0.75}}
# attractor = Attractor(Formula[input.formula], input.params)

emulatedFormula = (_params, x, y) ->
  # Formula.SimpleBlended([
  #   -1.0642758188769221,
  #   -1.3800999578088522,
  #   -0.1564171528443694,
  #   1.3192101996392012,
  #   1.8829933917149901,
  #   -0.13897148426622152,
  #   -0.150858367793262,
  #   1.5277614956721663,
  #   ,
  #   0.23145505599677563
  # ], x, y)

  weight = 0.13299989025108516
  a = Formula.Trigonometric([1, -1.0642758188769219, -0.15641715284436941, -1.0642758188769219, 1, -1.3800999578088522, 1.3192101996392012, -1.3800999578088522], x, y)
  b = Formula.Unnamed([1.88299339171499, -0.13897148426622152, -0.150858367793262, 1.5277614956721663], x, y)

  x: a.x * weight + b.x * 0.86700010974891484
  y: a.y * weight + b.y * 0.86700010974891484

# attractor = Attractor(emulatedFormula)
# attractor = Attractor(Formula.DeJong, [1.6623940085992217,-0.6880100890994072,1.4784153904765844,1.7967103328555822])
# attractor = Attractor(Formula.DeJong, [-1.9292301883127383,-1.7559409159631594,-1.8413772506711874,-1.972643807513176])
attractor = randomizeAttractor(Formula.SimpleBranched, Params.Standard)

zoomLevel = 2.0
viewZoomLevel = 2.0
centerPoint =
  x: 0
  y: 0
viewCenterPoint =
  x: 0
  y: 0

viewBounds = new Bounds(-viewZoomLevel + viewCenterPoint.x, viewZoomLevel + viewCenterPoint.x, -viewZoomLevel + viewCenterPoint.y, viewZoomLevel + viewCenterPoint.y)

reactor = new Reactor(attractor, {count: 50000})
reactor.onparticlemove = (particle, reactor) ->
  pos = particle.position
  vel = particle.velocity
  accel = particle.acceleration

  x = (pos.x - viewBounds.left) / viewBounds.width * positionGrid.size.width
  y = (pos.y - viewBounds.top) / viewBounds.height * positionGrid.size.height

  positionGrid.addXY(x|0, y|0)
  velocityGrid.addXY(x|0, y|0, Math.sqrt(vel.x * vel.x + vel.y * vel.y))
  accelerationGrid.addXY(x|0, y|0, Math.sqrt(accel.x * accel.x + accel.y * accel.y))

correctionCurve = StandardCurve(0.25, 0.5, 0.75)

@GridModifier =
  None: (gridMapper) -> gridMapper
  Corrected: (correctionCurve) ->
    (gridMapper) -> GridMapper.Corrected(gridMapper, correctionCurve)
  Inverted: (gridMapper) ->
    return (index) -> 1 - gridMapper(index)
  Multiplied: (constant, gridMapper) ->
    return (index) -> constant * gridMapper(index)
  Added: (constant, gridMapper) ->
    return (index) -> constant + gridMapper(index)
  Merged: (gridMapperA, gridMapperB) ->
    return (index) -> gridMapperA(index) + gridMapperB(index)

@Presets =
  Binary: ->
    PixelMapper.Monochrome GridMapper.Binary positionGrid
  Monochrome: (gridModifier) ->
    PixelMapper.Monochrome gridModifier GridMapper.Logarithmic positionGrid
  PositionLinear: (gridModifier) ->
    PixelMapper.Monochrome gridModifier GridMapper.Linear velocityGrid
  Velocity: (gridModifier) ->
    PixelMapper.Monochrome gridModifier GridMapper.Logarithmic velocityGrid
  Acceleration: (gridModifier) ->
    PixelMapper.Monochrome gridModifier GridMapper.Logarithmic accelerationGrid
  PVA: (gridModifier) ->
    modLog = (grid) -> gridModifier GridMapper.Logarithmic grid
    PixelMapper.RGB modLog(positionGrid), modLog(velocityGrid), modLog(accelerationGrid)
  APV: (gridModifier) ->
    modLog = (grid) -> gridModifier GridMapper.Logarithmic grid
    PixelMapper.RGB modLog(accelerationGrid), modLog(positionGrid), modLog(velocityGrid)
  VAP: (gridModifier) ->
    modLog = (grid) -> gridModifier GridMapper.Logarithmic grid
    PixelMapper.RGB modLog(velocityGrid), modLog(accelerationGrid), modLog(positionGrid)
  Classic: (gridModifier) ->
    # h = GridModifier.Multiplied(0.15, GridMapper.Logarithmic(accelerationGrid))
    # s = GridModifier.Inverted GridMapper.Linear velocityGrid
    # l = gridModifier GridMapper.Logarithmic positionGrid

    h = GridModifier.Multiplied(0.2, gridModifier GridMapper.Logarithmic velocityGrid)
    s = GridModifier.Added(0.6, GridModifier.Multiplied(0.4, GridMapper.Linear accelerationGrid))
    l = gridModifier GridMapper.Logarithmic positionGrid

    PixelMapper.HSL h, s, l
  DeepRed: (gridModifier) ->
    h = GridModifier.Added(-0.125, GridModifier.Multiplied(0.2, GridMapper.Logarithmic(accelerationGrid)))
    s = GridModifier.Multiplied(0.8, GridModifier.Inverted GridMapper.Linear velocityGrid)
    l = GridModifier.Multiplied(0.8, gridModifier GridMapper.Logarithmic positionGrid)

    PixelMapper.HSL h, s, l
  IceBlue: (gridModifier) ->
    h = GridModifier.Added(0.6, GridModifier.Multiplied(0.15, GridMapper.Logarithmic(accelerationGrid)))
    s = GridModifier.Multiplied(0.4, GridModifier.Inverted GridMapper.Linear velocityGrid)
    l = gridModifier GridMapper.Logarithmic positionGrid

    PixelMapper.HSL h, s, l
  # Emerald: (gridModifier) ->
  #   h = GridModifier.Added(0.24, GridModifier.Multiplied(0.07, GridMapper.Logarithmic(accelerationGrid)))
  #   s = GridModifier.Multiplied(0.6, GridModifier.Inverted GridMapper.Linear velocityGrid)
  #   l = GridModifier.Inverted gridModifier GridMapper.Logarithmic positionGrid

  #   PixelMapper.HSL h, s, l
  Emerald: (gridModifier) ->
    PixelMapper.Gradient(Gradient.Emerald, gridModifier GridMapper.Logarithmic positionGrid)
  Cherry: (gridModifier) ->
    PixelMapper.Gradient(Gradient.Cherry, gridModifier GridMapper.Logarithmic positionGrid)
  Testing: (gridModifier) ->
    h = GridModifier.Added(-0.35, GridModifier.Multiplied(0.45, gridModifier GridMapper.Logarithmic velocityGrid))
    # h = GridModifier.Multiplied(0.3, GridModifier.Added(-1, gridModifier GridMapper.Logarithmic velocityGrid))
    s = GridModifier.Added(0.6, GridModifier.Multiplied(0.4, GridMapper.Linear accelerationGrid))
    l = gridModifier GridMapper.Logarithmic positionGrid

    PixelMapper.HSL h, s, l

pixelMapper = Presets.APV GridModifier.None

running = false
renderingEnabled = true

run = ->
  reactor.step()
  renderer.render(pixelMapper) if renderingEnabled
  requestAnimationFrame(run) if running

toggle = $('Toggle')

setRunning = (r) ->
  running = r
  toggle.innerText = if running then 'Stop' else 'Start'
  requestAnimationFrame(run) if running

toggle.onclick = ->
  setRunning(!running)

$('Step').onclick = ->
  running = false
  run()

$('Save').onclick = ->
  window.open(canvas.toDataURL('image/png'))

refreshingOperation = (fn) ->
  fn()
  reactor.reset()
  positionGrid.clear()
  velocityGrid.clear()
  accelerationGrid.clear()
  renderer.render(pixelMapper) if renderingEnabled && !running

getState = ->
  formula: $('Formula').value
  params: reactor.attractor.params
  ttl: $('TTL').valueAsNumber
  bounds: reactor.bounds
  viewBounds: viewBounds
  preset: if $('Preset').value == 'Custom'
    color: $('Color').value
    colorShift: $('ColorShift').value
  else
    $('Preset').value
  inverted: $('Inverted').checked
  correction:
    enabled: $('Correction').checked
    a: $('CorrectionA').value * 0.01
    b: $('CorrectionB').value * 0.01
    c: $('CorrectionC').value * 0.01

showState = ->
  $('State').innerText = JSON.stringify getState()

extractZoomAndCenter = (bounds) ->
  x = (bounds.left + bounds.right) / 2
  y = (bounds.top + bounds.bottom) / 2

  # Validate zoom level
  z1 = bounds.right - x
  z2 = bounds.bottom - y
  z3 = x - bounds.left
  z4 = y - bounds.top

  if roughlyEquals(z1, z2) && roughlyEquals(z2, z3) && roughlyEquals(z3, z4)
    zoom: Math.round((-Math.log2(z1) + 5) * 2)
    centerX: x
    centerY: y
  else
    null

updateFromState = (json) ->
  shouldRefresh = false

  # Update formula
  validFormulas = Array.from($('Formula').options).map (o) -> o.value
  if validFormulas.includes(json.formula)
    $('Formula').value = json.formula

    # Update params if the formula is valid
    reactor.attractor = Attractor(Formula[json.formula], json.params)
    shouldRefresh = true

  # Update TTL
  if typeof json.ttl == 'number' && Number.isInteger(json.ttl)
    clampedTtl = Math.max(2, Math.min(500, json.ttl))
    $('TTLSlider').value = clampedTtl
    $('TTL').value = clampedTtl
    reactor.ttl = clampedTtl
    shouldRefresh = true

  # Update bounds
  if Bounds.areValid json.bounds
    zac = extractZoomAndCenter(json.bounds)
    if zac
      {zoom, centerX, centerY} = zac
      $('Zoom').value = zoom
      $('CenterX').value = centerX
      $('CenterY').value = centerY
      reactor.bounds = Bounds.fromZoomAndCenter(zoom, centerX, centerY)
      shouldRefresh = true
  if Bounds.areValid json.viewBounds
    zac = extractZoomAndCenter(json.viewBounds)
    if zac
      {zoom, centerX, centerY} = zac
      $('ViewZoom').value = zoom
      $('ViewX').value = centerX
      $('ViewY').value = centerY
      viewBounds = Bounds.fromZoomAndCenter(zoom, centerX, centerY)
      shouldRefresh = true
  $('SyncBounds').checked = false

  # Update rendering
  validPresets = Array.from($('Formula').options).map (o) -> o.value
  if validPresets.includes(json.preset)
    $('Preset').value = json.preset
    shouldRefresh = true
  else if json.preset && json.preset.color && json.preset.colorShift
    $('Preset').value = 'Custom'
    $('Color').value = json.preset.color
    $('ColorShift').value = json.preset.colorShift
    shouldRefresh = true

  if json.inverted != null
    $('Inverted').checked = !!json.inverted
    shouldRefresh = true

  if json.correction?.enabled
    $('Correction').checked = true
    if json.correction.a != null
      $('CorrectionA').value = Math.floor(json.correction.a * 100)
    if json.correction.b != null
      $('CorrectionB').value = Math.floor(json.correction.b * 100)
    if json.correction.c != null
      $('CorrectionC').value = Math.floor(json.correction.c * 100)
    shouldRefresh = true

  # Refresh the thing.
  if shouldRefresh
    refreshingOperation ->
      updateMapper()

updateFormula = ->
  formulaName = $('Formula').value
  paramsName = switch formulaName
    when 'DoubleTrigonometric' then 'Sixteen'
    when 'Quadratic'           then 'Sixteen'
    when 'GumowskiMira'        then 'GumowskiMira'
    else                            'Standard'

  refreshingOperation ->
    reactor.attractor = randomizeAttractor(Formula[formulaName], Params[paramsName])

  showState()

updateMapper = ->
  presetValue = $('Preset').value

  preset = if presetValue == 'Custom'
    $('CustomControls').className = "controls"
    createCustomMapper()
  else
    $('CustomControls').className = "controls hidden"
    Presets[presetValue]

  if $('Correction').checked
    a = $('CorrectionA').value * 0.01
    b = $('CorrectionB').value * 0.01
    c = $('CorrectionC').value * 0.01
    correctionCurve = StandardCurve(a, b, c)

    pixelMapper = preset GridModifier.Corrected correctionCurve
  else
    pixelMapper = preset GridModifier.None

  if $('Inverted').checked
    pixelMapper = PixelMapper.Inverse(pixelMapper)

  renderer.render(pixelMapper) if renderingEnabled && !running

  showState()

createCustomMapper = ->
  color = hex2rgb $('Color').value
  hsl = rgb2hsl(color.r, color.g, color.b)
  shift = $('ColorShift').valueAsNumber / 360

  return (gridModifier) ->
    h = GridModifier.Added(hsl.h, GridModifier.Multiplied(shift, GridMapper.Logarithmic velocityGrid))
    s = GridMapper.Constant hsl.s
    l = GridModifier.Multiplied(hsl.l, gridModifier GridMapper.Logarithmic(positionGrid))

    PixelMapper.HSL h, s, l

updateBounds = ->
  viewZoomLevel = $('ViewZoom').valueAsNumber
  viewCenterPoint =
    x: $('ViewX').valueAsNumber
    y: $('ViewY').valueAsNumber

  if $('SyncBounds').checked
    zoomLevel = viewZoomLevel
    centerPoint = viewCenterPoint
  else
    zoomLevel = $('Zoom').valueAsNumber
    centerPoint =
      x: $('CenterX').valueAsNumber
      y: $('CenterY').valueAsNumber

  refreshingOperation ->
    reactor.bounds = Bounds.fromZoomAndCenter(zoomLevel, centerPoint.x, centerPoint.y)
    viewBounds = Bounds.fromZoomAndCenter(viewZoomLevel, viewCenterPoint.x, viewCenterPoint.y)

  showState()

updateBoundsSync = ->
  sync = $('SyncBounds').checked

  $('Zoom').disabled = sync
  $('CenterX').disabled = sync
  $('CenterY').disabled = sync

  updateBounds()

updateTTL = ->
  ttl = $('TTL').valueAsNumber

  refreshingOperation ->
    reactor.ttl = ttl

  showState()

$('Rendering').onchange = ->
  renderingEnabled = $('Rendering').checked

  renderer.render(pixelMapper) if renderingEnabled && !running

$('Formula').onchange = updateFormula
$('Randomize').onclick = updateFormula
$('Preset').onchange = updateMapper
$('Inverted').onchange = updateMapper
$('Correction').onchange = updateMapper
$('CorrectionA').onchange = updateMapper
$('CorrectionB').onchange = updateMapper
$('CorrectionC').onchange = updateMapper
$('Zoom').onchange = updateBounds
$('CenterX').onchange = updateBounds
$('CenterY').onchange = updateBounds
$('ViewZoom').onchange = updateBounds
$('ViewX').onchange = updateBounds
$('ViewY').onchange = updateBounds
$('SyncBounds').onchange = updateBoundsSync

colorTimeout = null
$('Color').onchange = ->
  clearTimeout(colorTimeout)
  colorTimeout = setTimeout(updateMapper, 150)

$('ColorShiftSlider').onchange = ->
  $('ColorShift').value = $('ColorShiftSlider').value
  updateMapper()

$('ColorShift').onchange = ->
  $('ColorShiftSlider').value = $('ColorShift').value
  updateMapper()

$('TTLSlider').onchange = ->
  $('TTL').value = $('TTLSlider').value
  updateTTL()

$('TTL').onchange = ->
  $('TTLSlider').value = $('TTL').value
  updateTTL()

$('ResetBounds').onclick = (e) ->
  e.preventDefault()

  $('ViewZoom').value = 8
  $('ViewX').value = 0
  $('ViewY').value = 0

  updateBounds()

$('ResetCorrection').onclick = (e) ->
  e.preventDefault()

  $('CorrectionA').value = 25
  $('CorrectionB').value = 50
  $('CorrectionC').value = 75

  updateMapper()

showState()

# Debugging tools
window.updateFromState = updateFromState

# Handling of URL params
searchParams = new URLSearchParams(window.location.search)
creationId = searchParams.get 'creation'
shouldStop = searchParams.has 'stop'

setRunning(!shouldStop && creationId == null)

# Integration with pondiverse.com

import('https://www.pondiverse.com/pondiverse.js').then (p) ->
  p.addPondiverseButton ->
    type: 'chaoskit'
    data: JSON.stringify getState()
    image: canvas.toDataURL('image/png')

  if creationId != null
    p.fetchPondiverseCreation(creationId).then (creation) ->
      state = JSON.parse creation.data
      updateFromState state
      setRunning !shouldStop
    .catch ->
      setRunning !shouldStop
