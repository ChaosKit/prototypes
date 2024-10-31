// (function() {
function $(id) {
  return document.getElementById(id);
}
const canvas = $("C");
const ctx = canvas.getContext("2d");

/////////////////// Transform stuff

const identity = [1, 0, 0, 0, 1, 0];
const scale = (x, y) => [x, 0, 0, 0, y, 0];
const translate = (x, y) => [1, 0, x, 0, 1, y];
const rotate = angle => {
  const cos = Math.cos(angle);
  const sin = Math.sin(angle);
  return [cos, -sin, 0, sin, cos, 0];
};

function invert([a, b, c, d, e, f]) {
  const det = a * e - b * d;
  if (det === 0) {
    throw new Error("Transform is not invertible");
  }

  return [e, -b, b * f - c * e, -d, a, -a * f + c * d].map(el => el / det);
}

function multiplyTransforms(a, b) {
  const [a11, a12, a13, a21, a22, a23] = a;
  const [b11, b12, b13, b21, b22, b23] = b;

  return [
    a11 * b11 + a12 * b21,
    a11 * b12 + a12 * b22,
    a11 * b13 + a12 * b23 + a13,
    a21 * b11 + a22 * b21,
    a21 * b12 + a22 * b22,
    a21 * b13 + a22 * b23 + a23
  ];
}

function combine(...transforms) {
  switch (transforms.length) {
    case 0:
      return identity;
    case 1:
      return transforms[0];
    default:
      return transforms.reduce(
        (result, t) => multiplyTransforms(result, t),
        identity
      );
  }
}

function apply(point, ...transforms) {
  for (const [a, b, c, d, e, f] of transforms) {
    point = {
      x: a * point.x + b * point.y + c,
      y: d * point.x + e * point.y + f
    };
  }
  return point;
}

//////////////////////// Geometry stuff

const PROJECTION_TRANSFORM = combine(
  translate(canvas.width / 2, canvas.height / 2),
  scale(100, 100)
);
const INVERSE_PROJECTION_TRANSFORM = invert(PROJECTION_TRANSFORM);

function project(point) {
  return apply(point, PROJECTION_TRANSFORM);
}

function unproject(point) {
  return apply(point, INVERSE_PROJECTION_TRANSFORM);
}

function moveTo(point) {
  const transformed = project(point);
  ctx.moveTo(transformed.x, transformed.y);
}
function lineTo(point) {
  const transformed = project(point);
  ctx.lineTo(transformed.x, transformed.y);
}
function boundingBox(poly) {
  if (!poly.length) {
    throw new Error("Polygon has no points");
  }

  const box = {
    minX: poly[0].x,
    minY: poly[0].y,
    maxX: poly[0].x,
    maxY: poly[0].y
  };
  for (let i = 1; i < poly.length; i++) {
    const point = poly[i];
    box.minX = Math.min(box.minX, point.x);
    box.maxX = Math.max(box.maxX, point.x);
    box.minY = Math.min(box.minY, point.y);
    box.maxY = Math.max(box.maxY, point.y);
  }
  return box;
}

function pointInsideBox({ x, y }, { minX, maxX, minY, maxY }) {
  return x >= minX && x <= maxX && y >= minY && y <= maxY;
}

function pointInsidePoly(point, poly) {
  if (!pointInsideBox(point, boundingBox(poly))) {
    return false;
  }

  let isInside = false;
  const { x, y } = point;
  for (let i = 0, j = poly.length - 1; i < poly.length; j = i++) {
    const { x: ix, y: iy } = poly[i];
    const { x: jx, y: jy } = poly[j];

    if (iy > y !== jy > y && x < ((jx - ix) * (y - iy)) / (jy - iy) + ix) {
      isInside = !isInside;
    }
  }

  return isInside;
}

function pointInsideCircle(point, { center, radius }) {
  const x = point.x - center.x;
  const y = point.y - center.y;
  return x * x + y * y < radius * radius;
}

//////////////////// State

const state = {
  shapes: [],
  angle: 0,
  pointer: null,
  previousPointer: null
};

canvas.addEventListener("mousemove", e => {
  state.pointer = unproject({ x: e.offsetX, y: e.offsetY });
});
canvas.addEventListener("mouseout", () => {
  state.pointer = null;
});

class Shape {
  constructor() {
    this.parent = null;
    this.transform = identity;
    this.temporaryPreTransform = identity;
    this.temporaryPostTransform = identity;
    this.behaviors = new Map();
  }
  transformed(point) {
    return apply(point, this.getCombinedTransform());
  }
  getCombinedTransform() {
    return combine(
      this.temporaryPostTransform,
      this.transform,
      this.temporaryPreTransform,
      this.parent ? this.parent.getCombinedTransform() : identity
    );
  }
  draw() {
    throw new Error("Not implemented");
  }
  test(point) {
    throw new Error("Not implemented");
  }
  addBehavior(behavior) {
    const applied = behavior(this);
    this.behaviors.set(applied.id, applied);
    return this;
  }
  extendBehavior(name, object) {
    Object.assign(this.behaviors.get(name), object);
    return this;
  }
}

class Group extends Shape {
  constructor(elements) {
    super();
    this.elements = elements;
    for (const shape of elements) {
      shape.parent = this;
    }
  }
  draw() {
    for (const shape of this.elements) {
      shape.draw();
    }
  }
  test(point) {
    return this.elements.some(shape => shape.test(point));
  }
}

class Handle extends Shape {
  constructor(position) {
    super();
    this.position = position;
    this.radius = 5;
    this.addBehavior(Draggable);
  }
  draw() {
    const point = project(this.transformed(this.position));

    if (this.behaviors.get("draggable").isHovered) {
      ctx.fillStyle = "#ff0";
    } else if (this.parent.rotate) {
      ctx.fillStyle = "#00f";
    } else {
      ctx.fillStyle = "#f00";
    }

    ctx.beginPath();
    if (this.parent.rotate) {
      ctx.arc(point.x, point.y, this.radius, 0, Math.PI * 2);
    } else {
      ctx.rect(
        point.x - this.radius,
        point.y - this.radius,
        this.radius * 2,
        this.radius * 2
      );
    }
    ctx.fill();
  }
  test(point) {
    return pointInsideCircle(project(point), {
      center: project(this.transformed(this.position)),
      radius: this.radius
    });
  }
}

function Draggable(shape) {
  const behavior = {
    id: "draggable",
    isHovered: false,
    isDragging: false,
    startPoint: null,
    delta: null,
    onDragStart() {},
    onDrag() {},
    onDragEnd() {},
    onMouseMove() {}
  };

  canvas.addEventListener("mousedown", () => {
    if (!behavior.isHovered) return;
    behavior.isDragging = true;
    behavior.startPoint = state.pointer;
    behavior.delta = { x: 0, y: 0 };
    behavior.onDragStart(shape);
  });
  canvas.addEventListener("mouseup", () => {
    if (!behavior.isDragging) return;
    behavior.isDragging = false;
    behavior.onDragEnd(shape, behavior.delta);
  });
  canvas.addEventListener("mousemove", () => {
    if (!state.pointer) return;

    if (behavior.isDragging) {
      behavior.delta = {
        x: state.pointer.x - behavior.startPoint.x,
        y: state.pointer.y - behavior.startPoint.y
      };
      behavior.onDrag(shape, behavior.delta);
    } else {
      const isHovered = shape.test(state.pointer);
      const shouldDraw = isHovered !== behavior.isHovered;
      behavior.isHovered = isHovered;
      if (shouldDraw) drawAsync();
      if (isHovered) behavior.onMouseMove(shape);
    }
  });

  return behavior;
}

function Clickable(shape) {
  const behavior = {
    id: "clickable",
    onClick() {}
  };

  canvas.addEventListener("click", () => {
    if (shape.test(state.pointer)) {
      behavior.onClick(shape);
    }
  });

  return behavior;
}

const rect = Object.assign(new Shape(), {
  draw() {
    const points = [
      { x: -1, y: -1 },
      { x: 1, y: -1 },
      { x: 1, y: 1 },
      { x: -1, y: 1 }
    ].map(point => this.transformed(point));

    if (this.behaviors.get("draggable").isHovered) {
      ctx.strokeStyle = "#ccf";
      ctx.fillStyle = "#eef";
    } else {
      ctx.strokeStyle = "#ccc";
      ctx.fillStyle = "#eee";
    }

    ctx.beginPath();
    drawPoly(points);
    ctx.fill();
    ctx.stroke();
  },

  test(point) {
    return pointInsidePoly(
      point,
      [{ x: -1, y: -1 }, { x: 1, y: -1 }, { x: 1, y: 1 }, { x: -1, y: 1 }].map(
        point => this.transformed(point)
      )
    );
  }
})
  .addBehavior(Draggable)
  .addBehavior(Clickable);

const handles = [
  new Handle({ x: 1, y: 1 }), // bottom right
  new Handle({ x: 0, y: 1 }), // bottom center
  new Handle({ x: -1, y: 1 }), // bottom left
  new Handle({ x: -1, y: 0 }), // center left
  new Handle({ x: 1, y: 0 }), // center right
  new Handle({ x: 1, y: -1 }), // top right
  new Handle({ x: 0, y: -1 }), // top center
  new Handle({ x: -1, y: -1 }) // top left
];

const group = new Group([rect, ...handles]);

rect.extendBehavior("draggable", {
  onDragStart(shape) {
    group.temporaryPostTransform = identity;
  },
  onDrag(shape, delta) {
    group.temporaryPostTransform = translate(delta.x, delta.y);
    drawAsync();
  },
  onDragEnd(shape, delta) {
    group.transform = combine(translate(delta.x, delta.y), group.transform);
    group.temporaryPostTransform = identity;
    drawAsync();
  }
});
rect.extendBehavior("clickable", {
  onClick() {
    group.rotate = !group.rotate;
    drawAsync();
  }
});

for (const handle of handles) {
  handle.extendBehavior("draggable", {
    onDragStart(handle) {
      group.temporaryPreTransform = identity;
      group.temporaryPostTransform = identity;

      group.inverseTransform = invert(group.transform);
      group.handle = apply(
        { x: handle.position.x, y: handle.position.y },
        group.transform
      );
      group.oppositeHandle = apply(
        { x: -handle.position.x, y: -handle.position.y },
        group.transform
      );
      const leftTop = apply({ x: -1, y: -1 }, group.transform);
      const rightBottom = apply({ x: 1, y: 1 }, group.transform);
      group.width = rightBottom.x - leftTop.x;
      group.height = rightBottom.y - leftTop.y;

      handle.sign = {
        x: Math.sign(handle.position.x),
        y: Math.sign(handle.position.y)
      };
      handle.angle = Math.atan2(group.handle.y, group.handle.x);
    },
    onDrag(handle, delta) {
      if (group.rotate) {
        group.temporaryPreTransform = combine(
          // group.transform,
          rotate(
            Math.atan2(group.handle.y + delta.y, group.handle.x + delta.x) -
              handle.angle
          )
          // group.inverseTransform
        );
      } else {
        group.temporaryPostTransform = combine(
          group.transform,
          translate(group.oppositeHandle.x, group.oppositeHandle.y),
          scale(
            1 + (delta.x * handle.sign.x) / group.width,
            1 + (delta.y * handle.sign.y) / group.height
          ),
          translate(-group.oppositeHandle.x, -group.oppositeHandle.y),
          group.inverseTransform
        );
      }
      drawAsync();
    },
    onDragEnd(handle, delta) {
      group.transform = combine(
        group.temporaryPostTransform,
        group.transform,
        group.temporaryPreTransform
      );
      group.temporaryPreTransform = identity;
      group.temporaryPostTransform = identity;
      drawAsync();
    }
  });
}

state.shapes.push(group);

//////////////////// Drawing and stuff

function drawPoly(poly) {
  if (!poly.length) {
    return;
  }

  moveTo(poly[0]);
  for (let i = 1; i < poly.length; i++) {
    lineTo(poly[i]);
  }
  ctx.closePath();
}

function draw() {
  ctx.clearRect(0, 0, canvas.width, canvas.height);
  ctx.save();

  for (const shape of state.shapes) {
    shape.draw();
  }

  ctx.restore();
}

const drawAsync = (() => {
  let isWaiting = false;
  return () => {
    if (!isWaiting) {
      requestAnimationFrame(() => {
        draw();
        isWaiting = false;
      });
      isWaiting = true;
    }
  };
})();

draw();

function logic() {
  for (const shape of state.shapes) {
    shape.transform = combine(shape.transform, rotate((0.33 * Math.PI) / 180));
  }
}

function loop() {
  requestAnimationFrame(() => {
    logic();
    draw();
    loop();
  });
}
// loop();

// })();
