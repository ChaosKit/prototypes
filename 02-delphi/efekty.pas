unit efekty;

interface

uses Types, Graphics, Math;

procedure Blur(const bitmap: TPicture; const amount: Real);
procedure Glow(const bitmap: TPicture; const amount, radius: Real);

implementation

procedure Blur(const bitmap: TPicture; const amount: Real);
const
  SCALE = 2;
var
  sw, sh, scw, sch, steps, i: Integer;
  copy: TBitmap;
begin
  sw:= round(bitmap.Width / SCALE);
  sh:= round(bitmap.Height / SCALE);

  copy:= TBitmap.Create;
  copy.PixelFormat:= pf24bit;
  copy.Width:= sw;
  copy.Height:= sh;

  steps:= round(amount * 20);

  for i:= 0 to steps-1 do
  begin
    scw:= max(1, round(sw - i));
    sch:= max(1, round(sw - i));

    copy.Canvas.FillRect(Bounds(0,0,sw,sh));
    copy.Canvas.CopyRect(Bounds(0,0,scw,sch), bitmap.Bitmap.Canvas, Bounds(0,0,bitmap.Width,bitmap.Height));

    bitmap.Bitmap.Canvas.CopyRect(Bounds(0,0,bitmap.Width,bitmap.Height), copy.Canvas, Bounds(0,0,scw,sch));
  end;

  copy.Free;
end;

procedure Glow(const bitmap: TPicture; const amount, radius: Real);
const
  SCALE = 2;
var
  sw, sh, scw, sch, steps, i: Integer;
  copy, blur: TBitmap;
begin
  {blur:= TBitmap.Create;
  blur.PixelFormat:= pf24bit;
  blur.Width:= bitmap.Width;
  blur.Height:= bitmap.Height;
  blur.Canvas.CopyRect(Bounds(0,0,bitmap.Width,bitmap.Height), bitmap.Canvas, Bounds(0,0,bitmap.Width,bitmap.Height));

  sw:= round(bitmap.Width / SCALE);
  sh:= round(bitmap.Height / SCALE);

  copy:= TBitmap.Create;
  copy.PixelFormat:= pf24bit;
  copy.Width:= sw;
  copy.Height:= sh;

  steps:= round(radius * 20);

  for i:= 0 to steps-1 do
  begin
    scw:= max(1, round(sw - i));
    sch:= max(1, round(sw - i));

    copy.Canvas.CopyRect(Bounds(0,0,scw,sch), bitmap.Canvas, Bounds(0,0,bitmap.Width,bitmap.Height));

    blur.Canvas.CopyRect(Bounds(0,0,bitmap.Width,bitmap.Height), copy.Canvas, Bounds(0,0,scw,sch));
  end;   }
end;

end.
