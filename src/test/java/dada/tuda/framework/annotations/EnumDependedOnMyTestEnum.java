package dada.tuda.framework.annotations;

import dada.tuda.framework.redis.IEnum;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@Getter
@EnumBean
public enum EnumDependedOnMyTestEnum implements IEnum {
    FIRST,
    SECOND;
    @Autowired
    @Qualifier("myTestEnum.SECOND")
    MyTestEnum myTestEnum;


}