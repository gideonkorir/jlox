package com.craftinginterpreters.lox.visitors;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Data
@EqualsAndHashCode(callSuper = true)
public class LoxProperty extends LoxClassMember{
    @Getter
    public final String name;
    @Getter
    public final LoxFunction getter;
    @Getter
    public final LoxFunction setter;
}
