package com.yoloo.backend.rule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class RepeatRule implements TestRule {

  @Override public Statement apply(Statement base, Description description) {
    Statement result = base;
    Repeat repeat = description.getAnnotation(Repeat.class);

    if (repeat != null) {
      int times = repeat.times();
      result = new RepeatStatement(times, base);
    }
    return result;
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface Repeat {
    int times();
  }

  private static final class RepeatStatement extends Statement {
    private final int times;
    private final Statement statement;

    public RepeatStatement(int times, Statement statement) {
      this.times = times;
      this.statement = statement;
    }

    @Override public void evaluate() throws Throwable {
      for (int i = 0; i < times; i++) {
        statement.evaluate();
      }
    }
  }
}
