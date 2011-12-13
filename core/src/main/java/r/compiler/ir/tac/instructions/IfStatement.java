package r.compiler.ir.tac.instructions;

import java.util.Arrays;

import r.compiler.ir.tac.IRLabel;
import r.compiler.ir.tac.operand.SimpleExpr;
import r.lang.Context;
import r.lang.Logical;
import r.lang.SEXP;

public class IfStatement implements Statement, BasicBlockEndingStatement {
  
  private SimpleExpr condition;
  private IRLabel trueTarget;
  private IRLabel falseTarget;
  
  public IfStatement(SimpleExpr condition, IRLabel trueTarget, IRLabel falseTarget) {
    this.condition = condition;
    this.trueTarget = trueTarget;
    this.falseTarget = falseTarget;
  }

  public SimpleExpr getCondition() {
    return condition;
  }
  
  public IRLabel getTrueTarget() {
    return trueTarget;
  }

  public IRLabel getFalseTarget() {
    return falseTarget;
  }
  
  public IfStatement setTrueTarget(IRLabel label) {
    return new IfStatement(condition, label, falseTarget);
  }
  
  public IfStatement setFalseTarget(IRLabel label) {
    return new IfStatement(condition, trueTarget, label);
  }

  @Override
  public Iterable<IRLabel> possibleTargets() {
    return Arrays.asList(trueTarget, falseTarget);
  }

  @Override
  public Object interpret(Context context, Object[] temp) {
    boolean conditionalValue =  toBoolean(condition.retrieveValue(context, temp));
    if(conditionalValue) {
      return trueTarget;
    } else {
      return falseTarget;
    }
  }
  
  private boolean toBoolean(Object obj) {
    if(obj instanceof Boolean) {
      return (Boolean)obj;
    } else if(obj instanceof SEXP) {
      return ((SEXP)obj).asLogical() == Logical.TRUE;
    } else {
      throw new IllegalArgumentException(""+obj);
    }
  }

  @Override
  public String toString() {
    return "if " + condition + " goto " + trueTarget + " else " + falseTarget;
  }
}