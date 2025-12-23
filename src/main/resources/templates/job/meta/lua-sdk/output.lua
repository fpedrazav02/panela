---@meta
---@module "output"

---------------------------------------------------------
-- Output SDK module
---------------------------------------------------------

local output = {}

---Built-in: Write to a file
---@param config { path: string, from: string }
---@return table
function output.file(config) end

---Custom Lua: Write using a Lua script
---@param config { script: string, config: table, from: string }
---@return table
function output.lua(config) end

---Custom Java: Write using a Java class
---@param config { class: string, config: table, from: string }
---@return table
function output.java(config) end

return output