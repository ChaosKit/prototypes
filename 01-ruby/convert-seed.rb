#!/usr/bin/env ruby
print "Enter a seed: "
seed = gets.to_i

srand(seed)

ax = []
ay = []
6.times do |i|
  ax[i] = 4 * (rand() - 0.5)
  ay[i] = 4 * (rand() - 0.5)
end

puts "DeJong:"
puts ax[0]
puts ax[1]
puts ay[0]
puts ay[1]