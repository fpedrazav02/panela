package io.github.fpedrazav02.panela.validator.impl;

import io.github.fpedrazav02.panela.utils.Result;
import io.github.fpedrazav02.panela.validator.IValidator;

import java.util.regex.Pattern;

public class JobNameValidator implements IValidator<String> {

    private static final Pattern VALID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");
    private final String jobName;

    public JobNameValidator(String jobName) {
        this.jobName = jobName;
    }

    @Override
    public Result<String> validate() {
       if (jobName.isEmpty() || jobName.isBlank()) {
           return Result.fail("Job cannot be empty");
       }

       if (!VALID_PATTERN.matcher(jobName).matches()) {
           return Result.fail("Invalid job name");
       }
       return Result.ok(jobName);
    }
}
