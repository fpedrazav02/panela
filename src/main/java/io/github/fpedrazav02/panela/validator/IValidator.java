package io.github.fpedrazav02.panela.validator;

import io.github.fpedrazav02.panela.exceptions.custom.InvalidJobName;
import io.github.fpedrazav02.panela.utils.Result;

public interface IValidator<T> {
    public Result<T> validate();
}
