local echo = {}

function echo(spec)
    return {
        kind = "transform",
        type = "echo",
        from = spec.from,
        message = spec.message,
    }
end

return echo
