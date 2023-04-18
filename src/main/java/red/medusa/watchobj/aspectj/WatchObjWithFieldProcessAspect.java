package red.medusa.watchobj.aspectj;

/**
 * @date 2023/3/28
 * @see red.medusa.watchobj.ltw.LTWWatchObject
 */
// @Aspect
public class WatchObjWithFieldProcessAspect {

    // private final MutableJsonService handler = MutableJsonService.getInstance();
    //
    // @Pointcut("within(red.medusa.watchobj.example..*)")
    // public void pointcut() {
    // }
    //
    // @After(value = "pointcut() && initialization(*.new(..))")
    // public void initialization(JoinPoint joinPoint) {
    //     this.handler.watchObject(joinPoint.getTarget());
    // }
    //
    // @Before(value = "pointcut() && set(* *)")
    // public void beforeSetterPropertyValue() {
    //     this.handler.beforeRoll();
    // }
    //
    // @After(value = "pointcut() && set(* *)")
    // public void afterSetterPropertyValue(JoinPoint joinPoint) {
    //     Signature signature = joinPoint.getSignature();
    //     Object target = joinPoint.getTarget();
    //     String fieldName = signature.getName();
    //     Object value = joinPoint.getArgs()[0];
    //     Class<?> fieldType = ((FieldSignatureImpl) signature).getFieldType();
    //     this.handler.roll(target, fieldName, value, fieldType);
    // }
}
