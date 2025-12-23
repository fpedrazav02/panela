local job = require("job")
local input = require("input")
local transform = require("transform")

-- Declare inputs that can be referenced in transformations
local inputs = {
  showMe = input.value { data = "Hello World!", type = "string" }
}

-- Declare transformations that can be referenced in outputs
local transforms = {
  showStep = transform.echo {
    from = inputs.showMe,
    params = {},
  }
}

-- Declare outputs
local outputs = {}

return job.define {
  name = "%s",
  version = "0.1.0",
  inputs = inputs,
  transforms = transforms,
  outputs = outputs
}
