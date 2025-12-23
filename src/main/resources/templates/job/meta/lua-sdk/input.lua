---@meta
---@module "input"

---------------------------------------------------------
-- Input SDK module
---------------------------------------------------------

local input = {}

---Built-in: Create a value input
---@param config { data: any, type: string }
---@return table
function input.value(config) end

---Custom Lua: Load input from a Lua script
---@param config { script: string, config: table }
---@return table
function input.lua(config) end

---Custom Java: Load input from a Java class
---@param config { class: string, config: table }
---@return table
function input.java(config) end

return input