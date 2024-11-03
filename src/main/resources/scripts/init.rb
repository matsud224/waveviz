# --- --- Formatter --- ---
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

# --- --- Decoder --- ---
module DecoderPlugin
  def self.included(base)
    base.extend(DecoderPluginMetadata)
  end

  def name
    self.class.name
  end

  def description
    self.class.description
  end

  def version
    self.class.version
  end

  def waves
    self.class.waves
  end

  def annotations
    self.class.annotations
  end

  module DecoderPluginMetadata
    def decoder_info(name, description, version)
      @name, @description, @version = name, description, version
    end

    def name
      @name
    end

    def description
      @description
    end

    def version
      @version
    end

    def wave(sym, descr)
      (@wave ||= []) << [sym, descr]
    end

    def annotation(sym, descr)
      (@annotation ||= []) << [sym, descr]
    end

    def waves
      @wave
    end

    def annotations
      @annotation
    end
  end
end

class AXIStream
  include DecoderPlugin

  decoder_info 'axi-stream', 'AXI-Stream decoder', '1.0'

  wave :clock, 'Clock'
  wave :ready, 'Ready'
  wave :valid, 'Valid'
  wave :data,  'Data'

  annotation :transfer, 'Transfers on stream'
  annotation :error, 'Errors'

  def setup(wave)
    puts "Clock should be 1-bit width." if wave[:clock].width != 1
    puts "Ready should be 1-bit width." if wave[:ready].width != 1
    puts "Valid should be 1-bit width." if wave[:valid].width != 1
  end

  def decode(wave, annot)
    #wave.until_last do |wave|
    #  wave.wait({:clock => :rising_edge})
    #  if wave[:ready].high? && wave[:valid].high?
    #    annot[:transfer].add(wave.current_time, wave[:data])
    #  end
    #end
  end
end

Waveviz.register_decoder(AXIStream)
axis_decoder = AXIStream.new
puts axis_decoder.name
puts axis_decoder.version
puts axis_decoder.waves

puts 'Loaded init.rb'