local job       = require("job")
local input     = require("input")
local transform = require("transform")
local output    = require("output")

local inputs = {
  raw = input.file { path = "data/employees.csv", type = "csv" }
}

local transforms = {
  trimmed  = transform.trim       { from = "raw" },

  filled   = transform.fill_nulls { from = "trimmed", value = "N/A" },

  active   = transform.filter_rows {
    from   = "filled",
    column = "status",
    value  = "active",
    op     = "eq",
  },

  dropped  = transform.drop_columns {
    from    = "active",
    columns = { "status" },
  },

  casted   = transform.cast_column {
    from   = "dropped",
    column = "salary",
    type   = "integer",
  },

  sorted   = transform.sort_rows {
    from    = "casted",
    column  = "salary",
    order   = "desc",
    numeric = true,
  },
}

local outputs = {
  clean_employees = output.file { from = "sorted", format = "csv" }
}

return job.define {
  name    = "02-csv-cleaning",
  version = "0.1.0",
  inputs     = inputs,
  transforms = transforms,
  outputs    = outputs,
}

