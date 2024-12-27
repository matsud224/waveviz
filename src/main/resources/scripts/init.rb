#require_relative 'decoder'

# --- --- Formatter --- ---
def signal_to_int(value, base)
  i = Integer(value, 2, exception: false)
  if i.nil?
    'x'
  else
    i.to_s(base)
  end
end

Waveviz.register_formatter('Binary') { |value| value }
Waveviz.register_formatter('Octal') { |value| signal_to_int(value, 8) }
Waveviz.register_formatter('Decimal') { |value| signal_to_int(value, 10) }
Waveviz.register_formatter('Hexadecimal') { |value| signal_to_int(value, 16) }
Waveviz.register_formatter('Popcnt') { |value| value.count('1').to_s }

Waveviz.register_formatter('Sample-state') do |value|
  states = %w[WAIT PREP EXEC PAUSE WRITE]
  i = Integer(value, 2, exception: false)
  if i.nil?
    'x'
  elsif i >= states.length
    '---'
  else
    states[i]
  end
end

#mock = Waveviz::JavaMock.new
#mock.register_decoder('AXI-Stream', Waveviz::AXIStream)
#signal = Waveviz::SignalStore.new
#mock.decode('AXI-Stream', {
#              clock: signal.from_hier('top.clk'),
#              ready: signal.from_hier('top.mod1.ready'),
#              valid: signal.from_hier('top.mod1.valid'),
#              data: signal.from_hier(/top.mod1.data/)
#            })

# Waveviz.register_decoder(AXIStream)
# axis_decoder = AXIStream.new
# puts axis_decoder.name
# puts axis_decoder.version
# puts axis_decoder.waves

puts 'Loaded init.rb'
