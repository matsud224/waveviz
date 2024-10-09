def signal_to_int(value, base)
     i = Integer(value, 2, exception: false)
     if i == nil
         'x'
     else
         i.to_s(base)
     end
end

Waveviz.register_formatter('Binary') {|value| value}
Waveviz.register_formatter('Octal') {|value| signal_to_int(value, 8)}
Waveviz.register_formatter('Decimal') {|value| signal_to_int(value, 10)}
Waveviz.register_formatter('Hexadecimal') {|value| signal_to_int(value, 16)}

Waveviz.register_formatter('Sample-state') do |value|
    states = ['WAIT', 'PREP', 'EXEC', 'PAUSE', 'WRITE']
    i = Integer(value, 2, exception: false)
    if i == nil
        'x'
    elsif i >= states.length
        '---'
    else
        states[i]
    end
end

puts 'Loaded init.rb'