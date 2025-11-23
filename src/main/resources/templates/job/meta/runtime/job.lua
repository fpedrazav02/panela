-- .meta/runtime/job.lua

local JobRuntime = {}

-- Basic constructor
local function normalizeSpec(spec)
    return {
        name = spec.name,
        version = spec.version or "0.1.0",
        inputs = spec.inputs or {},
        transforms = spec.transforms or {},
        outputs = spec.outputs or {}
    }
end

--- The real implementation of job.define()
---@param spec table
---@return table
function JobRuntime.define(spec)
    if type(spec) ~= "table" then
        error("job.define: expected table, got " .. type(spec))
    end

    local job = normalizeSpec(spec)
    return job
end

return JobRuntime
