-- 令牌桶限流

-- 令牌的唯一标识
local bucketKey = KEYS[1]
-- 上次请求的时间
local last_mill_request_key = KEYS[2]
-- 令牌桶的容量
local limit = tonumber(ARGV[1])
-- 请求令牌的数量
local permits = tonumber(ARGV[2])
-- 令牌流入的速率
local rate = tonumber(ARGV[3])
-- 当前时间
local curr_mill_time = tonumber(ARGV[4])

-- 添加令牌

-- 获取当前令牌的数量
local current_limit = tonumber(redis.call('get', bucketKey) or "0")
-- 获取上次请求的时间
local last_mill_request_time = tonumber(redis.call('get', last_mill_request_key) or "0")
-- 计算向桶里添加令牌的数量
if last_mill_request_time == 0 then
	-- 令牌桶初始化
	-- 更新上次请求时间
	redis.call("HSET", last_mill_request_key, curr_mill_time)
	return 0
else
	local add_token_num = math.floor((curr_mill_time - last_mill_request_time) * rate)
end

-- 更新令牌的数量
if current_limit + add_token_num > limit then
    current_limit = limit
else
	current_limit = current_limit + add_token_num
end
	redis.pcall("HSET",bucketKey, current_limit)
-- 设置过期时间
redis.call("EXPIRE", bucketKey, 2)

-- 限流判断

if current_limit - permits < 1 then
    -- 达到限流大小
    return 0
else
    -- 没有达到限流大小
	current_limit = current_limit - permits
	redis.pcall("HSET", bucketKey, current_limit)
    -- 设置过期时间
    redis.call("EXPIRE", bucketKey, 2)
	-- 更新上次请求的时间
	redis.call("HSET", last_mill_request_key, curr_mill_time)
end

