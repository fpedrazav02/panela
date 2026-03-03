---@meta
---@module "output"

---------------------------------------------------------
-- Output SDK module
---------------------------------------------------------

local output = {}


---@alias FileType
---| "json"
---| "txt"
---| "csv"

---Built-in: Write to a file (written under build/ on the Java side)
---@param config { from: string, path?: string, format: FileType}
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