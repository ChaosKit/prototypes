#!/usr/bin/env ruby
require 'RMagick'
require 'getopt/std'
include Magick

def msleep(ms)
  sleep(ms * 0.001)
end

class Numeric
  def duration
    secs = self.to_int
    mins = secs / 60
    hours = mins / 60
    days = hours / 24

    if days > 0
      sprintf("#{days}d %02d:%02d:%02d", hours % 24, mins % 60, secs % 60)
    else
      sprintf("%02d:%02d:%02d", hours % 24, mins % 60, secs % 60)
    end
  end
end

class StrangeAttractor
  @@max_attractors = 10000

  private
    def next_attractor(fixedseed)
      done = false
      while !done
        @running = true
        @num_attractors += 1

        if @num_attractors > @@max_attractors then
          puts 'Osiągnięto limit atraktorów'
          @running = false
          return
        end

        @attr_timer = Time.now

        seed = fixedseed.nil? ? rand(0xffffffff) : fixedseed
        srand(seed)

        if @options[:output] == 'auto'
          @output = [@options[:formula].id2name, seed.to_s]
          @output.push(@options[:preset]) unless @options[:preset].nil?
        end

        print 'Wyliczanie atraktora (ziarno: ' + seed.to_s + ') '
        msleep(1)
        done = generate_attractor(!fixedseed.nil?)
        fixedseed = nil unless done
      end
    end
    def generate_attractor(fixed)
      ax = []
      ay = []
      xmin = 1e32
      xmax = -1e32
      ymin = 1e32
      ymax = -1e32

      6.times do |i|
        ax[i] = 4 * (rand() - 0.5)
        ay[i] = 4 * (rand() - 0.5)
      end

      x = [rand() - 0.5]
      y = [rand() - 0.5]
      hxy = [0]
      lyapunov = 0

      d0 = 0
      while d0 <= 0
        xe = x[0] + (rand() - 0.5) / 1000
        ye = y[0] + (rand() - 0.5) / 1000
        dx = x[0] - xe
        dy = y[0] - ye
        d0 = Math.sqrt(dx*dx + dy*dy)
      end

      i = 1
      batchcalc = 2500

      # nextcalc
      work = true
      while work
        fail = false
        j = 0
        while (i < @max_iterations) and (j < batchcalc)
          x1 = x[i-1]
          y1 = y[i-1]

          xx = x1*x1
          yy = y1*y1
          xy = x1*y1

          case @options[:formula]
          when :quadratic
            xi = ax[0] + ax[1]*x1 + ax[2]*xx + ax[3]*xy + ax[4]*y1 + ax[5]*yy
            yi = ay[0] + ay[1]*x1 + ay[2]*xx + ay[3]*xy + ay[4]*y1 + ay[5]*yy
          when :trig
            xi = ax[0] * Math.sin(ax[1]*y1) + ax[2] * Math.cos(ax[3]*x1)
            yi = ay[0] * Math.sin(ay[1]*x1) + ay[2] * Math.cos(ay[4]*y1)
          when :henon_old
            xi = y1 + 1 - 1.4 * xx
            yi = 0.3 * x1
          when :henon
            xi = y1 + 1 - ax[0] * xx
            yi = ay[0] * x1
          when :dejong_old
            xi = ax[0] * Math.sin(ax[1]*y1) - Math.cos(ax[2]*x1)
            yi = ay[0] * Math.sin(ax[1]*x1) - Math.cos(ax[2]*y1)
          when :dejong
            xi = Math.sin(ax[0]*y1) - Math.cos(ax[1]*x1)
            yi = Math.sin(ay[0]*x1) - Math.cos(ay[1]*y1)
          when :tinkerbell
            xi = xx - yy + ax[0]*x1 + ax[1]*y1
            yi = 2 * xy + ay[0]*x1 + ay[1]*y1
          when :ikeda
            t = 0.4 - 6 / (1 + xx + yy)
            xi = 1 + ax[0] * (x1 * Math.cos(t) - y1 * Math.sin(t))
            yi = ax[0] * (x1 * Math.sin(t) + y1 * Math.cos(t))
          when :rossler
            xi = x1 - y1
            yi = y1 + x1 + ay[0]*y1
          end

          x[i] = xi
          y[i] = yi

          # Update the bounds
          xmin = xi if xi < xmin
          ymin = yi if yi < ymin
          xmax = xi if xi > xmax
          ymax = yi if yi > ymax

          if @options[:compositing]
            dx = (xi - x1) / (xmax - xmin)
            dy = (yi - y1) / (ymax - ymin)
            hxy[i] = dx*dx + dy*dy
          end

          unless fixed
            # Does the series tend to INFINITY
            if (xmin < -1e10) or (ymin < -1e10) or (xmax > 1e10) or (ymax > 1e10)
              fail = true
              break
            end

            xexe = xe*xe
            xeye = xe*ye
            yeye = ye*ye

            case @options[:formula]
            when :quadratic
              xenew = ax[0] + ax[1]*xe + ax[2]*xexe + ax[3]*xeye + ax[4]*ye + ax[5]*yeye
              yenew = ay[0] + ay[1]*xe + ay[2]*xexe + ay[3]*xeye + ay[4]*ye + ay[5]*yeye
            when :trig
              xenew = ax[0] * Math.sin(ax[1]*ye) + ax[2] * Math.cos(ax[3]*xe)
              yenew = ay[0] * Math.sin(ay[1]*xe) + ay[2] * Math.cos(ay[4]*ye)
            when :henon_old
              xenew = ye + 1 - 1.4 * xexe
              yenew = 0.3 * xe
            when :henon
              xenew = ye + 1 - ax[0] * xexe
              yenew = ay[0] * xe
            when :dejong_old
              xenew = ax[0] * Math.sin(ax[1]*ye) - Math.cos(ax[2]*xe)
              yenew = ay[0] * Math.sin(ax[1]*xe) - Math.cos(ax[2]*ye)
            when :dejong
              xenew = Math.sin(ax[0]*ye) - Math.cos(ax[1]*xe)
              yenew = Math.sin(ay[0]*xe) - Math.cos(ay[1]*ye)
            when :tinkerbell
              xenew = xexe - yeye + ax[0]*xe + ax[1]*ye
              yenew = 2 * xeye + ay[0]*xe + ay[1]*ye
            when :ikeda
              t = 0.4 - 6 / (1 + xexe + yeye)
              xenew = 1 + ax[0] * (xe * Math.cos(t) - ye * Math.sin(t))
              yenew = ax[0] * (xe * Math.sin(t) + ye * Math.cos(t))
            when :rossler
              xenew = xe - ye
              yenew = ye + xe + ay[0]*ye
            end

            # Does the series tend to a point
            dx = x[i] - x[i-1]
            dy = y[i] - y[i-1]
            if (dx.abs() < 1e-10) and (dy.abs() < 1e-10)
              fail = true
              break
            end

            # Calculate the Lyapunov exponent
            if i > 1000
              dx = x[i] - xenew
              dy = y[i] - yenew
              dd = Math.sqrt(dx*dx + dy*dy)
              lyapunov += Math.log((dd/d0).abs())
              xe = x[i] + d0 * dx / dd
              ye = y[i] + d0 * dy / dd
            end
          end

          i += 1
          j += 1
        end

        if (i < @max_iterations) and !fail
          print '.'
          msleep(1)
        else
          work = false

          # calcdone
          puts
          unless fixed
            # Classify the series according to Lyapunov
            unless fail
              if lyapunov.abs() < 10
                puts 'Znaleziono szereg neutralnie stabilny, niedobrze'
                fail = true
              elsif lyapunov < 0
                puts sprintf('Znaleziono szereg okresowy (współczynnik %.6f), niedobrze', lyapunov)
                fail = true
              else
                puts sprintf('Znaleziono szereg chaotyczny (współczynnik %.6f)', lyapunov)
              end
            end
          end

          if (!fail) or fixed
            print 'Rysowanie atraktora '
            msleep(1)
            draw(x, y, xmin, xmax, ymin, ymax, hxy)
          end
        end
      end
      return !fail
    end

    def draw(x, y, xmin, xmax, ymin, ymax, hxy)
      bg = @background
      cnv = Image.new(@w, @h) { self.background_color = bg }
      blur = Image.new(@w, @h) { self.background_color = bg }

      finish = lambda do
        now = Time.now
        puts 'Gotowe!'
        puts 'Czasy: skrypt - ' + (now - @start_timer).duration + ', atraktor - ' + (now - @attr_timer).duration

        name = @output.join('-') + '.png'
        puts 'Wygenerowany atraktor znajduje się w pliku ' + name if @options[:output] == 'auto'
        @running = false
        cnv.write(name)
      end

      xrange = (xmax - xmin) / 0.8
      yrange = (ymax - ymin) / 0.8

      unless @options[:stretch]
        if yrange > xrange
          xmin -= (yrange - xrange) / 2
          xmax -= (yrange - xrange) / 2
          xrange = yrange
        elsif xrange > yrange
          ymin -= (xrange - yrange) / 2
          ymax -= (xrange - yrange) / 2
          yrange = xrange
        end
      end

      hue = Integer(rand(360 * 4) % 360)
      #blurctx.fill('hsla('+hue.to_s+',100%,60%,0.05)')

      batchcalc = 2000
      dc = 0
      i = 0

      work = true
      while work
        c = 1.5 / @options[:quality]
        bc = (10 * @options[:quality]).to_i
        dc += 1
        j = 0
        while (j < batchcalc) and (i < @max_iterations)
          fx = (x[i] - xmin) / xrange + 0.1
          fy = (y[i] - ymin) / yrange + 0.1
          ix = (fx * @w).to_i
          iy = (fy * @h).to_i

          if i > 100
            p = cnv.pixel_color(ix, iy)

            r = p.red + c
            g = p.green + c
            b = p.blue + c

            r = 255 if r > 255
            g = 255 if g > 255
            b = 255 if b > 255

            cnv.pixel_color(ix, iy, sprintf('rgb(%d,%d,%d)', r.round(), g.round(), b.round()))

            if @options[:compositing]
              if i < batchcalc*25
                bhue = Integer((hue + hxy[i] * 120) % 360)

                blurctx = Draw.new
                blurctx.fill('hsla('+bhue.to_s+',100%,60%,0.075)')

                off = 5
                bx = ix - off
                by = iy - off

                blurctx.rectangle(bx, by, bx+(2*off), by+(2*off))
                blurctx.draw(blur)
              end
            end
          end
          i += 1
          j += 1
        end

        if i < @max_iterations
          print '.'
          msleep(1)
        else
          work = false

          # drawdone
          puts
          if @options[:compositing] then
            print 'Kolorowanie... '
            msleep(100)

            # composite
            bg = @background
            color = Image.new(@w, @h) { self.background_color = bg }

            # pierwsze co trzeba zrobic to zblurowac blura
            puts '(1/4)'
            msleep(50)
            blurred = blur.blur_image(0.0, 5.0)
            # teraz pokolorujemy atraktor blurem
            colored = cnv.composite(blurred, 0, 0, ColorizeCompositeOp)

            puts 'Kolorowanie... (2/4)'
            msleep(50)
            # nastepnie trza wziac i lekko rozmyc atraktor
            bluratr = cnv.blur_image(0.0, 2.5)
            # musimy dodac atraktor do obrazka zeby ladnie swiecil
            colored.composite!(bluratr, 0, 0, LinearDodgeCompositeOp)

            puts 'Kolorowanie... (3/4)'
            msleep(50)
            # i zeby jeszcze bardziej swiecil
            glow = colored.blur_image(0.0, 10.0).blend(color, (1.5 ** -@options[:composite_level]))
            cnv = colored.composite(glow, 0, 0, LinearDodgeCompositeOp)

            puts 'Kolorowanie... (4/4)'
            msleep(50)

            # na fajne kolory tla, ktore jednak czasem moga byc zbyt hardkorowe
            back = cnv.blur_image(0.0, Math.sqrt(@w*@h)/8)
            back = back.blend(color, 0.5)

            cnv.composite!(back, 0, 0, LinearDodgeCompositeOp)

            # i wyszlo zajebiscie!
            finish.call
          else
            finish.call
          end
        end
      end
    end

  public
    def initialize(options={})
      @start_timer = Time.now
      @options = {
        :width => 512,
        :height => 512,
        :formula => :dejong,
        :seed => nil,
        :compositing => false,
        :stretch => true,
        :quality => 0.2,
        :output => 'auto',
        :preset => nil,
        :composite_level => 1
      }.merge(options)

      @w = @options[:width]
      @h = @options[:height]
      @background = '#000'
      @ratio = (@w*@h)/262144
      @max_iterations = (2000000 * @options[:quality] * @ratio).to_i
      @num_attractors = 0

      next_attractor(@options[:seed])
    end

    def running?
      return @running
    end
end

# Presets! (wow)
presets = {
  :draft => { :quality => 0.01 },
  :lq => { :quality => 0.2, :compositing => true },
  :mq => { :quality => 0.5, :compositing => true },
  :hq => { :quality => 1, :compositing => true },
  :sag => { :width => 640, :height => 640, :quality => 1, :compositing => true },
  :superhq => { :width => 1024, :height => 1024, :quality => 1, :compositing => true }
  #:ultrahq => { :width => 2048, :height => 2048, :quality => 2, :compositing => true } # 64bit only
}

# Command line!
opts = Getopt::Std.getopts('p:s:f:n:l:q:o:CS')
genopts = {}

if opts['p']
  genopts = presets[opts['p'].intern]
  genopts[:preset] = opts['p']
end

genopts[:compositing] = genopts[:compositing] ? !genopts[:compositing] : true if opts['C']
genopts[:stretch] = genopts[:stretch] ? !genopts[:stretch] : false if opts['S']
genopts[:formula] = opts['f'].intern if opts['f']
genopts[:seed] = opts['n'].to_i if opts['n']
genopts[:quality] = opts['q'].to_f if opts['q']
genopts[:output] = opts['o'] if opts['o']
genopts[:composite_level] = opts['l'].to_f if opts['l']

if opts['s'] # size
  size = opts['s'].split('x')
  genopts[:width] = size[0].to_i
  genopts[:height] = size[1].to_i
end

STDOUT.sync = true
gen = StrangeAttractor.new(genopts)
