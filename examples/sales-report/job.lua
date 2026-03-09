local job       = require("job")
local input     = require("input")
local transform = require("transform")
local output    = require("output")

local inputs = {
  raw = input.file { path = "data/orders.csv", type = "csv" }
}

local transforms = {
  trimmed = transform.trim { from = "raw" },

  selected = transform.select_columns {
    from    = "trimmed",
    columns = { "order_id", "customer_name", "product", "category", "unit_price", "country" },
  },

  renamed = transform.rename_columns {
    from   = "selected",
    rename = {
      order_id      = "id",
      customer_name = "customer",
      unit_price    = "price",
    },
  },

  electronics = transform.filter_rows {
    from   = "renamed",
    column = "category",
    value  = "Electronics",
    op     = "eq",
  },

  tagged = transform.add_column {
    from        = "electronics",
    name        = "price_tag",
    from_column = "price",
    expression  = "copy",
  },

  deduped = transform.deduplicate {
    from    = "tagged",
    columns = { "id" },
  },

  sorted = transform.sort_rows {
    from    = "deduped",
    column  = "price",
    order   = "desc",
    numeric = true,
  },
}

local outputs = {
  electronics_report = output.file { from = "sorted", format = "csv" }
}

return job.define {
  name    = "03-sales-report",
  version = "0.1.0",
  inputs     = inputs,
  transforms = transforms,
  outputs    = outputs,
}

