function last(arr) {
  return arr[arr.length - 1];
}
function shuffle(a) {
  var j, x, i;
  for (i = a.length - 1; i > 0; i--) {
    j = Math.floor(Math.random() * (i + 1));
    x = a[i];
    a[i] = a[j];
    a[j] = x;
  }
  return a;
}

const things = [
  { weight: 1, count: 0 },
  { weight: 2, count: 0 },
  { weight: 3, count: 0 },
  { weight: 5, count: 0 },
  { weight: 8, count: 0 },
];

const weightSum = things.reduce((sum, { weight }) => sum + weight, 0);

console.log(
  "Expected probabilities:",
  things.map(({ weight }) => weight / weightSum)
);

const NUM_ITERATIONS = 1000;
const NUM_PARTICLES = 100;

console.group("Algorithm 1: Picking");
for (const thing of things) {
  thing.count = 0;
}

(function() {
  const limits = things.reduce(
    (limits, { weight }) => [
      ...limits,
      (limits.length ? last(limits) : 0) + weight,
    ],
    []
  );
  // console.log("Limits:", limits);

  for (let i = 0; i < NUM_ITERATIONS; i++) {
    for (let j = 0; j < NUM_PARTICLES; j++) {
      const limit = Math.random() * weightSum;
      for (let k = 0; k < limits.length; k++) {
        if (limit < limits[k]) {
          things[k].count++;
          break;
        }
      }
    }
  }

  console.log(
    "Results:",
    things.map((thing) => ({
      count: thing.count,
      probability: thing.count / (NUM_ITERATIONS * NUM_PARTICLES),
    }))
  );
})();

console.groupEnd();

console.group("Algorithm 2: Shuffling");
for (const thing of things) {
  thing.count = 0;
}

(function() {
  const indices = things.map((_, i) => i);

  for (let i = 0; i < NUM_ITERATIONS; i++) {
    shuffle(indices);

    let current = 0;
    let switchAtParticle = Math.round(
      (things[indices[current]].weight / weightSum) * NUM_PARTICLES
    );
    for (let j = 0; j < NUM_PARTICLES; j++) {
      things[indices[current]].count++;
      if (j === switchAtParticle) {
        current++;
        if (current < indices.length) {
          switchAtParticle += Math.round(
            (things[indices[current]].weight / weightSum) * NUM_PARTICLES
          );
        } else {
          switchAtParticle = NUM_PARTICLES;
          current--;
        }
      }
    }
  }

  console.log(
    "Results:",
    things.map((thing) => ({
      count: thing.count,
      probability: thing.count / (NUM_ITERATIONS * NUM_PARTICLES),
    }))
  );
})();

console.groupEnd();
