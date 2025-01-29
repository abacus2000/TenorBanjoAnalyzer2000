package org.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

// handler value: example.HandlerInteger - note for testing in lambda 

/*
 * preserving this class for potential simple teseting for issues with lambda itself 
 */

public class HandlerIntegerJava17 implements RequestHandler<IntegerRecord, Integer>{

  @Override
  public Integer handleRequest(IntegerRecord event, Context context)
  {
    LambdaLogger logger = context.getLogger();
    logger.log("String found: " + event.message());
    return event.x() + event.y();
  }
}

record IntegerRecord(int x, int y, String message) {
}