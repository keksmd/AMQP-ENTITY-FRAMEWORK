package dada.tuda.framework.enums;

import dada.tuda.framework.annotations.EnumBean;
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