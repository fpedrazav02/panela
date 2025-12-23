local echo = {}

function echo(spec)
    return {
        kind = "transform",
        type = "echo",
        from = spec.from,
        params = spec.params,
    }
end

return echo
