local job       = require("job")
local input     = require("input")
local transform = require("transform")
local output    = require("output")

local inputs = {
  greeting = input.value { data = "Hello from Panela!", type = "string" }
}

local transforms = {
  echo = transform.echo { from = "greeting", params = {} }
}

local outputs = {
  result = output.file { from = "echo", format = "txt" }
}

return job.define {
  name    = "hello-world",
  version = "0.1.0",
  inputs     = inputs,
  transforms = transforms,
  outputs    = outputs,
}

