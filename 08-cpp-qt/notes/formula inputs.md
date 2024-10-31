# Possible AST types

* Root — 0+ Assignments, Formula | PiecewiseFormula
* Formula — 2 Expressions
* Assignment — variable name = Expression | PiecewiseExpression
  * in c++ something would return a Variable that could be used
* Expression
  * Boolean Expression?
* Input — x, y
* Number — any float
* Variable
* Coefficients of the pre affine transform — a, b, c, d, e, f — pointer to params
* Params — indexed by number
* Weight — of the variation — pointer to params
* Random — sign (delta), pi (omega), number(0–1) (psi)
* PiecewiseExpression — list of pairs [Boolean Expression, Expression]
* Unary Function — sin, cos, tan, unary minus, not, sqrt, atan, trunc, exp, floor, ceil, signum, abs
* Binary Function — plus, minus, mul, div, pow, and, or, lt, gt, eq, lte, gte, mod, distance

Helpers:

* r = distance(Input) -> sqrt(x^2 + y^2) or native implementation
* rho(Input) -> atan(x/y)
* phi(Input) -> atan(y/x)
* pi() -> well, pi

Formula params can be typed as floats or ints and named.
Probably should declare that as a vector of descriptors of sth.
The AST receives only floats.

# Example formula — Ngon (v38)

p1 = power
p2 = 2pi / sides
p3 = corners
p4 = circle

t3 = phi - p2 * floor(phi/p2)
t4 = t3      for t3 >  p2/2
     t3 - p2 for t3 <= p2/2
k = (p3 * (1 / cos(t4) - 1) + p4) / r^p1

V(x,y) = k * (x,y)

## AST

Assignment(phi)
  UnaryFunction(atan)
    BinaryFunction(div)
      Input(y)
      Input(x)
Assignment(t3)
  BinaryFunction(minus)
    Variable(phi)
    BinaryFunction(mul)
      Parameter(2)
      UnaryFunction(floor)
        BinaryFunction(div)
          Variable(phi)
          Parameter(2)
Assignment(t4)
  Piece
    Variable(t3)
    BinaryFunction(gt)
      Variable(t3)
      BinaryFunction(div)
        Parameter(2)
        Number(2)
  Otherwise
    BinaryFunction(minus)
      Variable(t3)
      Parameter(2)
Assignment(k)
  ...
Formula
  x: BinaryFunction(mul)
    Variable(k)
    Input(x)
  y: BinaryFunction(mul)
    Variable(k)
    Input(y)
