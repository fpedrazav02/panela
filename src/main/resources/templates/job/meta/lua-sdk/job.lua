---@meta
---@module "job"

---------------------------------------------------------
-- JobSpec type definition
---------------------------------------------------------

---@class JobSpec
---@field name string              # Job name
---@field version? string         # Semantic version
---@field inputs? table<string, any>
---@field transforms? table<string, any>
---@field outputs? table<string, any>

---------------------------------------------------------
-- Job type definition
---------------------------------------------------------

---@class Job
---@field name string
---@field version string
---@field inputs table<string, any>
---@field transforms table<string, any>
---@field outputs table<string, any>

---------------------------------------------------------
-- job SDK module (stub)
---------------------------------------------------------

local job = {}

---Define a Job
---@param spec JobSpec
---@return Job
function job.define(spec)
	-- Stub should be filled in runtime
end

return job
