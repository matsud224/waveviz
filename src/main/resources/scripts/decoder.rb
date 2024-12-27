class String
  def high?
    self == '1'
  end

  def low?
    self == '0'
  end
end

module Waveviz
  module DecoderPlugin
    def self.included(base)
      base.extend(ClassMethods)
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

    def inputs
      self.class.inputs
    end

    def outputs
      self.class.outputs
    end

    module ClassMethods
      def decoder_info(description, version)
        @description = description
        @version = version
      end

      def description
        @description
      end

      def version
        @version
      end

      def input(sym, descr)
        (@inputs ||= []) << [sym, descr]
      end

      def output(sym, descr)
        (@outputs ||= []) << [sym, descr]
      end

      def inputs
        @inputs
      end

      def outputs
        @outputs
      end
    end
  end

  class InputSignals
    def initialize(input)
      @input = input
    end
  end

  class DecodeContext
    def initialize(input)
      @input = input
      @time = 0
    end

    def [](sig)
      @input[sig].value_at(@time)
    end

    attr_accessor :time

    def last_time
      @input.map { |sig| sig[1].last_time }.max
    end

    def finished?
      last_time < @time
    end

    def skip(duration)
      @time += duration
    end

    def wait(*conditions)
      signals = Set.new
      conditions.each do |cond|
        cond.each do |sig, _|
          signals << sig
        end
      end

      loop do
        next_edge = signals.map do |sig|
          [sig, @input[sig].next_edge(@time)]
        end.filter { |x| !x[1].nil? }.min_by { |x| x[1] }

        if next_edge.nil?
          @time = last_time + 1
          break nil
        end

        next_edge_time = next_edge[1]
        @time = next_edge_time

        results = conditions.map do |cond|
          cond.map do |sig, type|
            case type
            when :pos_edge
              self[sig].pos_edge?
            when :neg_edge
              self[sig].neg_edge?
            when :edge
              self[sig].edge?
            when :high
              self[sig].high?
            when :low
              self[sig].low?
            end
          end.all?
        end

        break results if results.any?
      end
    end
  end

  class AXIStream
    include DecoderPlugin

    decoder_info 'AXI-Stream', '1.0'

    input :clock, 'Clock'
    input :ready, 'Ready'
    input :valid, 'Valid'
    input :data,  'Data'

    output :annot, 'Annotations'

    def setup(input)
      raise 'Clock should be 1-bit width.' unless input[:clock].width == 1
      raise 'Ready should be 1-bit width.' unless input[:ready].width == 1
      raise 'Valid should be 1-bit width.' unless input[:valid].width == 1
    end

    def decode(input, output)
      input.until_last do |ctx|
        if ctx.wait({ clock: :pos_edge }) && (ctx[:valid].high? && ctx[:ready].high?)
          output[:annot] << [ctx.time, { type: :transfer, data: ctx[:data] }]
        end
      end
    end
  end

  # test
  class JavaMock
    def initialize
      @decoders = {}
    end

    def register_decoder(name, klass)
      @decoders[name] = klass
    end

    def decode(name, input)
      input.each do |k, v|
        if v.is_a?(Array)
          puts "Error: input signal '#{k}' is array."
          return
        end
        if v.nil?
          puts "Error: input signal '#{k}' is nil."
          return
        end
      end

      decoder = @decoders[name].new
      decoder.setup(input) if decoder.respond_to?(:setup)
      output = Hash.new([])

      def input.until_last
        ctx = DecodeContext.new(self)
        yield ctx until ctx.finished?
      end

      decoder.decode(input, output)
      pp output[:annot]
    end
  end

  class Signal
    def initialize(value_changes)
      @value_changes = value_changes
    end

    def last_time
      @value_changes[-1][0]
    end

    def width
      @value_changes.map { |c| c[1].length }.max
    end

    def unknown_value
      'x' * width
    end

    def value_at(time)
      return unknown_value if @value_changes.empty?
      return unknown_value if @value_changes[0][0] > time

      next_index = @value_changes.bsearch_index do |x|
        x[0] > time
      end

      current_index = if !next_index
                        @value_changes.length - 1
                      else
                        next_index - 1
                      end

      section_time, value = @value_changes[current_index]

      edge = section_time == time

      if edge
        prev_index = current_index - 1
        value = if prev_index >= 0
                  @value_changes[prev_index][1]
                else
                  unknown_value
                end
      end

      unless value.singleton_methods.include?(:edge?)
        value.define_singleton_method :edge? do
          edge
        end
        value.define_singleton_method :pos_edge? do
          edge && self == '0'
        end
        value.define_singleton_method :neg_edge? do
          edge && self == '1'
        end
      end

      value
    end

    def next_edge(time)
      next_edge_time, = @value_changes.bsearch do |x|
        x[0] > time
      end
      next_edge_time
    end
  end

  class SignalStore
    def initialize
      @signals = {}
      @signals['top.clk'] =
        Signal.new([[0, '0'], [10, '1'], [20, '0'], [30, '1'], [40, '0'], [50, '1'], [60, '0']])
      @signals['top.mod1.ready'] = Signal.new([[0, '0'], [20, '1'], [50, '0']])
      @signals['top.mod1.valid'] = Signal.new([[0, '0'], [20, '1'], [50, '0']])
      @signals['top.mod1.data']  = Signal.new([[20, '0101'], [30, '1010'], [50, '0000']])
    end

    def from_hier(hier)
      if hier.is_a?(Regexp)
        list = @signals.keys.grep(hier).map do |h|
          @signals[h]
        end
        if list.empty?
          nil
        elsif list.length == 1
          list[0]
        else
          list
        end
      else
        @signals[hier]
      end
    end
  end
end
