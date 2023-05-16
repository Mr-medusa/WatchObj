package red.medusa.watchobj.aspectj;

import red.medusa.watchobj.core.MutableJsonService;
import red.medusa.watchobj.ltw.LTWWatchObject;

/**
 * 观察实例及其字段处理的 Aspect,当对象实例化时或字段更新会触发 MutableJsonService 服务
 *
 * @date 2023/3/28
 * @see LTWWatchObject
 * @see MutableJsonService
 */
// @Aspect
public class WatchObjWithFieldProcessAspect {

    // private final MutableJsonService handler = MutableJsonService.getInstance();
    // 配置需要观察的切入点
    // @Pointcut("within(red.medusa.watchobj.example..*)")
    // public void pointcut() {
    // }
    //
    // @After(value = "pointcut() && initialization(*.new(..))")
    // public void initialization(JoinPoint joinPoint) {
    // 对象初始化时开观察对象
    //     this.handler.watchObject(joinPoint.getTarget());
    // }

    /**
     * 字段值更新之后触发属性设置
     * @see MutableJsonService#beforeRoll()
     */
    // @Before(value = "pointcut() && set(* *)")
    // public void beforeSetterPropertyValue() {
    //     this.handler.beforeRoll();
    // }
    //

    /**
     * 字段值更新之后触发属性设置
     * @see MutableJsonService#roll(Object, String, Object, Class)
     */
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
