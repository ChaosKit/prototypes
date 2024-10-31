# rough blueprint

- generator
  - start with opencl
  - then cpu? cuda? api has to work with all
- renderer
  - opengl
  - something offline, probably cuda/opencl, maybe cpu someday
- variation library
  - register(name, fn) — fn: point -> params -> node
  - need an API for nodes (see below)
  - start with a bundle of common nodes (all the flame paper ones)
  - idea: build a parser, need a unregister function then (actually for all user-changable ones)
    - actually it's stupid, if somebody is going to put math/code in the editor, they could build a simple plugin
    - idea: add JS or lua or something one day ;P
- node API - NOT a plugin, builtin

# random ideas

- single plugin implementing multiple APIs (e.g. one plugin implementation both the CL generator and renderer)
- add user data void* to callbacks — **important**

# let's do a C API

## generator

purpose: transform points in a loop and emit them
needs:

- point list
- random seeds (optional, can manage alone)
- transform tree (node)
- transform params array (float)
- ttl (int)

state managed inside the generator, needs to provide setters (and maybe a getter for the point array to do interop)

ok, so we have different pipelines: full gpu, cpu -> gpu, full cpu
in the full gpu case, how do we handle keeping the state on the gpu?

  - handle types; think a pointer / int with a 4-byte tag
    - if the renderer supports this type of handle, all is well
    - no need for configs, declarations or flags

plugins should be configurable! a generator should know if it should get the data back from the gpu or not.
there needs to be a registration routine that lets the core know.

maybe something like this:

```c
struct ck_plugin_config {
  ck_plugin_config_key key;
  union {
    int i;
    float f;
  } value;
};

struct ck_plugin {
  ck_plugin_type type; // bitset for GENERATOR, RENDERER, others

  char[4] id;
  char* name;
  int name_length;

  ck_plugin_config* config;
  int config_size;
};

void ck_plugin_set_i(ck_plugin_config_key key, int value);
void ck_plugin_set_f(ck_plugin_config_key key, float value);
void ck_plugin_register(ck_plugin* plugin);
```

so what's there to do:

- `void ck_generator_init()` — does any initialization it needs
- `void ck_generator_free()` — does any deinitialization it needs
- `void ck_generator_config_i(enum, value)` — sets a configuration variable
  - enum is:
    - `CK_POINTS_PER_STEP` - 0
    - `CK_LOOPS_PER_STEP` - 1
    - anything custom will be 1024+
- `void ck_generator_set_transforms(node)`
- `void ck_generator_set_ttl(int)`
- `void ck_generator_set_params(float*, int size)`
- `void ck_generator_set_step_callback(void(void*, void*) cb)`
- `void ck_generator_reset()` – resets the internal state, randomizes points etc.
- `void ck_generator_step()`
- `void ck_generator_step_sync()` — same as above, but ensures the memory is local

## renderer

purpose: add points to grid, run final transforms, fetch tonemapped grid
needs:
- point list
- final transform node
- final transform params
- palette

methods (merge common ones?)

- `void ck_renderer_init()` — does any initialization it needs
- `void ck_renderer_free()` — does any deinitialization it needs
- `void ck_renderer_config_i(enum, value)` — sets a configuration variable
  - `CK_IMAGE_WIDTH` - 16
  - `CK_IMAGE_HEIGHT` - 17
