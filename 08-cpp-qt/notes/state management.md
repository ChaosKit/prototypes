use cases:

- programmatic use cases
  - building the tree nicely
  - generating the AST
- UI use cases
  - supporting the QAbstractModelView
  - moving things around (drag'n'drop)
- generating a trimmed down System for the editor
- manipulating through operations (for undo/redo)

# example operation implementations for different structures

## AddBlend()

### normalized

up:
newB = blends.emplace("id", {...})
return {"id"} // affected items
down:
blend.erase("id"); // takes ID stored somewhere inside
return {"id"}

can be generalized!

### nested

up:
system.blends.emplace_back({...})
down:
system.blends.pop_back()

## AddFormula(type, blend)

### normalized

up:
newF = formulas.emplace("id", {type: type, parent: blend.id})
