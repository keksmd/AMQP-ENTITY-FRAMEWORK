package dada.tuda.framework.redis;

import dada.tuda.framework.annotations.EnumBean;

@EnumBean
public class DemoNoEnumClass implements IEnum {
	@Override
	public String name() {
		return " and that some class";
	}
}
