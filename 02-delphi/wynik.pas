unit wynik;

interface

uses
  Windows, Messages, SysUtils, Variants, Classes, Graphics, Controls, Forms,
  Dialogs, ExtCtrls, StdCtrls, MT19937, ComCtrls, GBlur2, HSLUtils, ExtDlgs, info;

const
  MAXATTR = 10000;
  STEP = 2000;

type
  PRGBArray = ^TRGBArray;
  TRGBArray = array[Word] of TRGBTriple;

  PRealValue = ^TRealValue;
  TRealValue = record
    value: Double;
    next: PRealValue;
  end;

  TRealList = class(TPersistent)
  private
    start, last: PRealValue;
    cnt: Integer;
  public
    constructor Create;
    destructor Destroy; override;

    procedure Add(const val: Double);
    function Remove: Double;

    function GetList: PRealValue;
  published
    property Count: Integer read cnt;
  end;

  TAttractorThread = class(TThread)
  private
    w,h,maxiter,num,funkcja,fseed, composite: Integer;
    stretch: Boolean;
    q, colorshift: Double;
    timer: TDateTime;

    procedure Rysuj(const x,y: TRealList; xmin, xmax, ymin, ymax: Double; const hxy: TRealList);
    function GenerujAtraktor(const gen: Boolean): Boolean;
    function NowyAtraktor: Integer;
  protected
    procedure Execute; override;
  public
    constructor Create(const seed, f, fx: Integer; const quality, shift: Double; const stretch: Boolean);

    function IsRunning: Boolean;
    procedure Stop;
  end;

  TImageForm = class(TForm)
    Image: TImage;
    SaveDialog: TSavePictureDialog;
    procedure FormCreate(Sender: TObject);
    procedure FormClose(Sender: TObject; var Action: TCloseAction);
  public
    th: TAttractorThread;
    currseed: Integer;
    procedure ZrobAtraktor(const w,h,seed, f, fx: Integer; const quality, shift: Double; const stretch: Boolean);
  end;

var
  ImageForm: TImageForm;


implementation

{$R *.dfm}

// Dodawanie do ListBoxa - �eby kod byl kr�tszy
procedure AddToLog(log: TListBox; const str: String);
begin
  log.items.Add(str);
  log.ItemIndex:= log.Count - 1;
  log.Repaint;
end;

//////////////////////////////////////////////////////////// FUNKCJE KOLOROWANIA
{ Rysuje prostok�t z o danym stopniu przezroczystosci }
procedure Prostokat(bitmap: TBitmap; const x,y,w,h,r,g,b: Integer; const a: Double);
var
  i,j: Integer;
  row: PRGBArray;
begin
  for j:= y to y+h-1 do
  begin
    row:= bitmap.ScanLine[j];
    for i:= x to x+w-1 do
    begin
      row[i].r:= trunc(row[i].r + (r - row[i].r) * a);
      row[i].g:= trunc(row[i].g + (g - row[i].g) * a);
      row[i].b:= trunc(row[i].b + (b - row[i].b) * a);
    end;
  end;
end;

{ Koloruje obrazek na podstawie innego }
function Colorize(const a,b: TBitmap): TBitmap;
var
  i,j: Integer;
  ha,sa,la,hb,sb,lb: Double;
  rescol, acol, bcol: TColor;
  resrow, arow, brow: PRGBArray;
begin
  Result:= TBitmap.Create;
  Result.Width:= a.Width;
  Result.Height:= a.Height;
  Result.PixelFormat:= pf24bit;

  for j:= 0 to a.Height - 1 do
  begin
    resrow:= Result.ScanLine[j];
    arow:= a.ScanLine[j];
    brow:= b.ScanLine[j];

    for i:= 0 to a.Width - 1 do
    begin
      acol:= rgb(arow[i].r, arow[i].g, arow[i].b);
      bcol:= rgb(brow[i].r, brow[i].g, brow[i].b);

      RGBtoHSL(acol, ha,sa,la);
      RGBtoHSL(bcol, hb,sb,lb);

      rescol:= HSLtoRGB(ha, sa, lb);

      resrow[i].r:= GetRValue(rescol);
      resrow[i].g:= GetGValue(rescol);
      resrow[i].b:= GetBValue(rescol);
    end;
  end;
end;

{ Dodaje kolory dw�ch obrazk�w do siebie }
procedure LinearDodgeSelf(const src: TBitmap; var dest: TBitmap);
var
  i,j, r,g,b: Integer;
  arow, brow: PRGBArray;
begin
  for j:= 0 to src.Height - 1 do
  begin
    arow:= src.ScanLine[j];
    brow:= dest.ScanLine[j];

    for i:= 0 to src.Width - 1 do
    begin
      r:= arow[i].r + brow[i].r;
      g:= arow[i].g + brow[i].g;
      b:= arow[i].b + brow[i].b;

      if r > 255 then r:= 255;
      if g > 255 then g:= 255;
      if b > 255 then b:= 255;

      brow[i].r:= r;
      brow[i].g:= g;
      brow[i].b:= b;
    end;
  end;
end;

{ Przyciemnia obrazek w podanym stopniu }
procedure FadeSelf(var dest: TBitmap; const amount: Double);
var
  i,j: Integer;
  brow: PRGBArray;
begin
  for j:= 0 to dest.Height - 1 do
  begin
    brow:= dest.ScanLine[j];

    for i:= 0 to dest.Width - 1 do
    begin
      brow[i].r:= trunc(brow[i].r - brow[i].r * amount);
      brow[i].g:= trunc(brow[i].g - brow[i].g * amount);
      brow[i].b:= trunc(brow[i].b - brow[i].b * amount);
    end;
  end;
end;

//////////////////////////////////////////////////////////////////// LISTA LICZB
constructor TRealList.Create;
begin
  start:= nil;
  last:= nil;
  cnt:= 0;
end;
destructor TRealList.Destroy;
begin
  last:= nil;
  while start <> nil do Remove;
  start:= nil;
end;

procedure TRealList.Add(const val: Double);
var el: PRealValue;
begin
  New(el);
  el^.value:= val;
  el^.next:= nil;

  if start = nil then start:= el
  else last^.next:= el;
  last:= el;
end;

function TRealList.GetList;
begin
  Result:= start;
end;

function TRealList.Remove;
var wywal: PRealValue;
begin
  Result:= start^.value;
  wywal:= start;
  start:= start^.next;
  Dispose(wywal);
end;

///////////////// G��wny form
procedure TImageForm.FormCreate(Sender: TObject);
begin
  currseed:= -1;
  with Image.Picture.Bitmap do
  begin
    PixelFormat:= pf24bit;
    Width:= Image.Width;
    Height:= Image.Height;
  end;

  Image.Picture.Bitmap.Canvas.Brush.Color:= clBlack;
  Image.Picture.Bitmap.Canvas.FillRect(ImageForm.Image.ClientRect);
end;

procedure TImageForm.FormClose(Sender: TObject; var Action: TCloseAction);
var
  q: Integer;
begin
  if th.IsRunning then
  begin
    q:= MessageBox(Handle, 'Czy na pewno chcesz przerwa� generowanie atraktora?', 'Generator dziwnych atraktor�w', MB_YESNO + MB_DEFBUTTON2 + MB_ICONQUESTION);
    case q of
      IDYES:
      begin
        th.Stop;
        AddToLog(InfoForm.Log, 'Przerwano.');
        InfoForm.Progress.Position:= 0;
      end;
      IDNO: Action:= caNone;
    end;
  end;
  InfoForm.Close;
end;

procedure TImageForm.ZrobAtraktor(const w,h,seed, f, fx: Integer; const quality, shift: Double; const stretch: Boolean);
begin
  if not InfoForm.running then
  begin
    ClientWidth:= w;
    ClientHeight:= h;

    Image.Picture.Bitmap.Width:= w;
    Image.Picture.Bitmap.Height:= h;

    Show;

    // Ustawia formularz z logiem w odpowiednim miejscu
    if (Top + Height + InfoForm.Height) <= (Screen.Height - 32) then
    begin
      InfoForm.Top:= Top + Height;
      InfoForm.Left:= Left;
    end;

    th:= TAttractorThread.Create(seed, f, fx, quality, shift, stretch);
    th.FreeOnTerminate:= true;
  end;
end;

//////////////////////////////////////////////// W�A�CIWE GENEROWANIE ATRAKTOR�W
constructor TAttractorThread.Create(const seed, f, fx: Integer; const quality, shift: Double; const stretch: Boolean);
begin
  Randomize;

  self.w:= ImageForm.Image.Width;
  self.h:= ImageForm.Image.Height;
  self.fseed:= seed;
  self.maxiter:= trunc(2000000 * quality * (self.w*self.h)/262144);
  self.funkcja:= f;
  self.q:= quality;
  self.stretch:= stretch;
  self.composite:= fx;
  self.timer:= Now;
  self.colorshift:= shift;

  InfoForm.Show;
  InfoForm.Caption:= 'Generowanie atraktora...';

  InfoForm.Log.Items.Clear;
  InfoForm.Progress.Min:= 0;
  InfoForm.Progress.Max:= self.maxiter;
  InfoForm.Progress.Step:= 1;

  inherited Create(false);
end;

procedure TAttractorThread.Execute;
begin
  try
    ImageForm.currseed:= NowyAtraktor;
  except
    on e: EOverflow do
    begin
      MessageBox(ImageForm.Handle, 'W trakcie generowania wyst�pi� b��d. Prawdopodobnie podany numer atraktora jest nieprawid�owy dla danej funkcji.'+#13#10+'Podaj inny numer lub pozostaw pole pustym.', 'Generator dziwnych atraktor�w', MB_OK + MB_ICONERROR + MB_DEFBUTTON1);
      InfoForm.running:= false;
      ImageForm.Close;       
    end;
  end;
end;

procedure TAttractorThread.Stop;
begin
  InfoForm.running:= false;
  Terminate;
end;

function TAttractorThread.IsRunning: Boolean;
begin
  Result:= InfoForm.running;
end;

function TAttractorThread.NowyAtraktor: Integer;
var
  seed: Integer;
  done: Boolean;
  caption: String;
begin
  done:= false;
  Result:= -1;
  while not done and not Terminated do
  begin
    InfoForm.running:= true;
    inc(num);

    // Jest limit atraktor�w, �eby nie lecia� w k�ko
    if num > MAXATTR then
    begin
      AddToLog(InfoForm.Log, 'Osi�gni�to limit atraktor�w, przerywanie');
      InfoForm.running:= false;
      Result:= -1;
      Exit;
    end;

    // Losowanie numeru
    if fseed = -1 then
      seed:= Random($0fffffff)
    else
      seed:= fseed;
    sgenrand_MT19937(seed);

    // Tytu� formularza
    case funkcja of
    0: caption:= 'Kwadratowa (';
    1: caption:= 'Trygonometryczna (';
    2: caption:= 'Henon (';
    3: caption:= 'Peter de Jong Mod (';
    4: caption:= 'Peter de Jong (';
    5: caption:= 'Tinkerbell (';
    6: caption:= 'Ikeda (';
    end;
    caption:= caption + inttostr(seed) + ')';
    ImageForm.Caption:= caption;
    
    InfoForm.Progress.Position:= 0;
    AddToLog(InfoForm.Log, Format('Generowanie atraktora (numer: %d)...', [seed]));

    done:= GenerujAtraktor((fseed = -1));
    if not done then fseed:= -1;
    
    Result:= seed;
  end;

  Stop;
end;

function TAttractorThread.GenerujAtraktor(const gen: Boolean): Boolean;
var
  ax, ay: array [0..5] of Double;
  xmin, xmax, ymin, ymax: Double;
  d0,dd,dx,dy,lapunow: Double;
  xe,ye,nowexe,noweye: Double;
  i, etap: Integer;
  loop, fail: Boolean;

  j: Integer;
  hxy, x1, y1, xx, yy, xy, xi, yi, xexe, xeye, yeye, absdx, absdy, absddd0, t: Double;
begin
  Result:= true;

  // Czyszczenie obrazka
  with ImageForm.Image.Picture.Bitmap.Canvas do
  begin
    Brush.Color:= clBlack;
    FillRect(ImageForm.Image.ClientRect);
  end;

  // Losowanie parametr�w
  for i:=0 to 5 do
  begin
    ax[i]:= 4 * (randFloat_MT19937 - 0.5);
    ay[i]:= 4 * (randFloat_MT19937 - 0.5);
  end;

  // Od teraz dzialamy bez list!!!111
  x1 := randFloat_MT19937 - 0.5;
  y1 := randFloat_MT19937 - 0.5;

  lapunow:= 0;
  xmin:= 1e32;
  xmax:= -1e32;
  ymin:= 1e32;
  ymax:= -1e32;

  xe:= x1 + (randFloat_MT19937 - 0.5) / 1000;
  ye:= y1 + (randFloat_MT19937 - 0.5) / 1000;
  dx:= x1 - xe;
  dy:= y1 - ye;
  d0:= sqrt(dx*dx + dy*dy);

  i:= 1;

  for etap:= 1 to 2 do
  begin
  // G��wna p�tla
  loop:= true;
  repeat
    fail:= false;
    j:= 0;
    // Generowanie etapami (�eby nie od�wie�ac tak cz�sto)
    while (j < STEP) and (i < maxiter) and not Terminated do
    begin
      x1:= lx^.value;
      y1:= ly^.value;
      xx:= x1*x1;
      yy:= y1*y1;
      xy:= x1*y1;

      // Dodawanie wartosci szeregu
      xi:= 0;
      yi:= 0;
      case funkcja of
        0: // Kwadratowa
        begin
          xi:= ax[0] + ax[1]*x1 + ax[2]*xx + ax[3]*xy + ax[4]*y1 + ax[5]*yy;
          yi:= ay[0] + ay[1]*x1 + ay[2]*xx + ay[3]*xy + ay[4]*y1 + ay[5]*yy;
        end;
        1: // Trygonometryczna
        begin
          xi:= ax[0] * sin(ax[1]*y1) + ax[2] * cos(ax[3]*x1);
          yi:= ay[0] * sin(ay[1]*x1) + ay[2] * cos(ay[3]*y1);
        end;
        2: // Henon
        begin
          xi:= y1 + 1 - 1.4*xx;
          yi:= 0.3 * x1;
        end;
        3: // de Jong - zmodyfikowany
        begin
          xi:= ax[0] * sin(ax[1]*y1) - cos(ax[2]*x1);
          yi:= ay[0] * sin(ax[1]*x1) - cos(ax[2]*y1);
        end;
        4: // de Jong
        begin
          xi:= sin(ax[0]*y1) - cos(ax[1]*x1);
          yi:= sin(ay[0]*x1) - cos(ay[1]*y1);
        end;
        5: // Tinkerbell
        begin
          xi:= xx - yy + ax[0]*x1 + ax[1]*y1;
          yi:= 2*xy + ay[0]*x1 + ay[1]*y1;
        end;
        6: // Ikeda
        begin
          t:= 0.4 - 6 / (1 + xx + yy);
          xi:= 1 + ax[0] * (x1*cos(t) - y1*sin(t));
          yi:= ax[0] * (x1*sin(t) + y1*cos(t));
        end;
      end;

      x.Add(xi);
      y.Add(yi);

      // Wyliczanie nowych wartosci granicznych
      if xi < xmin then xmin:= xi;
      if yi < ymin then ymin:= yi;
      if xi > xmax then xmax:= xi;
      if yi > ymax then ymax:= yi;

      // Wyliczanie wartosci w celu kolorowania wg "szybkosci" punktow
      if (composite > 0) and (i > 100) then
      begin
        dx:= (xi-x1) / (xmax-xmin);
        dy:= (yi-y1) / (ymax-ymin);
        hxy.Add(dx*dx+dy*dy);
      end;

      // Sprawdzanie numeru tylko je�eli jest losowy
      if gen then
      begin
        // Czy szereg d��y do niesko�czono�ci
        if (xmin < -1e10) or (ymin < -1e10) or (xmax > 1e10) or (ymax > 1e10) then
        begin
          fail:= true;	
          Break;
        end;

        xexe:= xe*xe;
        xeye:= xe*ye;
        yeye:= ye*ye;
        case funkcja of
          0: // Kwadratowa
          begin
            nowexe:= ax[0] + ax[1]*xe + ax[2]*xexe + ax[3]*xeye + ax[4]*ye + ax[5]*yeye;
            noweye:= ay[0] + ay[1]*xe + ay[2]*xexe + ay[3]*xeye + ay[4]*ye + ay[5]*yeye;
          end;
          1: // Trygonometryczna
          begin
            nowexe:= ax[0] * sin(ax[1]*ye) + ax[2] * cos(ax[3]*xe);
            noweye:= ay[0] * sin(ay[1]*xe) + ay[2] * cos(ay[3]*ye);
          end;
          2: // Henon
          begin
            nowexe:= ye + 1 - 1.4*xexe;
            noweye:= 0.3 * xe;
          end;
          3: // de Jong - zamodyfikowany
          begin
            nowexe:= ax[0] * sin(ax[1]*ye) - cos(ax[2]*xe);
            noweye:= ay[0] * sin(ax[1]*xe) - cos(ax[2]*ye);
          end;
          4: // de Jong
          begin
            nowexe:= sin(ax[0]*ye) - cos(ax[1]*xe);
            noweye:= sin(ay[0]*xe) - cos(ay[1]*ye);
          end;
          5: // Tinkerbell
          begin
            nowexe:= xexe - yeye + ax[0]*xe + ax[1]*ye;
            noweye:= 2*xeye + ay[0]*xe + ay[1]*ye;
          end;
          6: // Ikeda
          begin
            t:= 0.4 - 6 / (1 + xexe + yeye);
            nowexe:= 1 + ax[0] * (xe*cos(t) - ye*sin(t));
            noweye:= ax[0] * (xe*sin(t) + ye*cos(t));
          end;
        end;

        // Czy szereg d��y do punktu
        dx:= lx^.next^.value - x1;
        dy:= ly^.next^.value - y1;
        if (abs(dx) < 1e-10) and (abs(dy) < 1e-10) then
        begin
          fail:= true;
          Break;
        end;

        // Liczenie wyk�adnika Lapunowa
        if i>1000 then
        begin
          dx:= lx^.next^.value - nowexe;
          dy:= ly^.next^.value - noweye;
          dd:= sqrt(dx*dx + dy*dy);
          lapunow:= lapunow + ln(abs(dd / d0));
          xe:= lx^.next^.value + d0 * dx / dd;
          ye:= ly^.next^.value + d0 * dy / dd;
        end;
      end;
      
      lx:= lx^.next;
      ly:= ly^.next;
      inc(i);
      inc(j);
    end;

    if (i < maxiter) and not fail then
    begin
      InfoForm.Progress.StepBy(STEP);
    end
    else
    begin
      // Generowanie zako�czone, sprawdzanie wykladnika Lapunowa
      if gen then
        if not fail then
        begin
          if abs(lapunow) < 10 then
          begin
            AddToLog(InfoForm.Log, 'Znaleziono szereg neutralnie stabilny, przerywanie');
            fail:= true;
          end
          else if lapunow < 0 then
          begin
            AddToLog(InfoForm.Log, 'Znaleziono szereg okresowy, przerywanie');
            fail:= true;
          end
          else
            AddToLog(InfoForm.Log, 'Znaleziono szereg chaotyczny');
        end;

      loop:= false;
    end;
  until (not loop) or Terminated;

  // Przejscie do rysowania
  if ((not fail) or (not gen)) and not Terminated then
  begin
    AddToLog(InfoForm.Log, 'Rysowanie atraktora...');
    Rysuj(x,y,xmin,xmax,ymin,ymax,hxy);
  end;

  lx:= nil;
  ly:= nil;
  x.Free;
  y.Free;
  hxy.Free;

  Result:= not fail;
end;

procedure TAttractorThread.Rysuj(const x,y: TRealList; xmin, xmax, ymin, ymax: Double; const hxy: TRealList);
var
  i, ix, iy, r,g,b: Integer;
  hue, bhue, fx, fy, xrange, yrange: Double;
  blur, colored, blurred, bluratr, cnv, glow, back: TBitmap;
  loop: Boolean;

  j, c, off, bx, by: Integer;
  col: TColor;
  row: PRGBArray;
begin
  InfoForm.Progress.Position:= 0;

  // Przygotowanie obrazk�w
  blur:= TBitmap.Create;
  with blur do
  begin
    Width:= w;
    Height:= h;
    PixelFormat:= pf24bit;
    Canvas.Brush.Color:= clBlack;
    Canvas.FillRect(ImageForm.Image.ClientRect);
  end;

  cnv:= TBitmap.Create;
  with cnv do
  begin
    Width:= w;
    Height:= h;
    PixelFormat:= pf24bit;
    Canvas.Brush.Color:= clBlack;
    Canvas.FillRect(ImageForm.Image.ClientRect);
  end;

  // Zakres rysowania
  xrange:= (xmax-xmin) / 0.8;
  yrange:= (ymax-ymin) / 0.8;

  // Dostosowanie zakresu je�eli nie rozci�gamy atraktora
  if not stretch then
  begin
    if yrange > xrange then
    begin
      xmin:= xmin - (yrange-xrange)/2;
      xmax:= xmax - (yrange-xrange)/2;
      xrange:= yrange;
    end
    else if xrange > yrange then
    begin
      ymin:= ymin - (xrange-yrange)/2;
      ymax:= ymax - (xrange-yrange)/2;
      yrange:= xrange;
    end;
  end;

  // Wyliczanie koloru pocz�tkowego
  hue:= randFloat_MT19937 + colorshift;
  while hue > 1 do hue:= hue - 1;

  i:= 0;
  loop:= true;
  repeat
    c:= round(1.5 / q);

    j:=0;
    while (j < STEP) and (i < maxiter) and not Terminated do
    begin
      // Wyliczanie wsp�lrz�dnych punktu na ekranie
      fx:= (x.Remove - xmin) / xrange + 0.1;
      fy:= (y.Remove - ymin) / yrange + 0.1;
      ix:= trunc(fx*w);
      iy:= trunc(fy*h);

      if i > 100 then
      begin
        // Rysowanie punktu
        row:= cnv.ScanLine[iy];
        r:= row[ix].r + c;
        g:= row[ix].g + c;
        b:= row[ix].b + c;
        if r > 255 then r:= 255;
        if g > 255 then g:= 255;
        if b > 255 then b:= 255;

        row[ix].r:= r;
        row[ix].g:= g;
        row[ix].b:= b;

        // Rysowanie prostok�ta w odpowiednim kolorze na osobnym obrazku
        if (composite > 0) then
          if i < STEP*25 then
          begin
            bhue:= hue + hxy.Remove / 3;
            while bhue > 1 do bhue:= bhue - 1;

            col:= HSLtoRGB(bhue, 1, 0.6);

            off:= 5;
            bx:= trunc(ix-off);
            by:= trunc(iy-off);
            Prostokat(blur, bx, by, 2*off, 2*off, GetRValue(col), GetGValue(col), GetBValue(col), 0.075);
          end;
      end;

      inc(i);
      inc(j);
    end;

    if i < maxiter then
    begin
      if (i mod (STEP*10)) = 0 then
        ImageForm.Image.Picture.Graphic:= cnv;

      InfoForm.Progress.StepBy(STEP);
    end
    else
    begin
      ImageForm.Image.Picture.Graphic:= cnv;
      if (composite > 0) then
      begin
        // Kolorowanie
        AddToLog(InfoForm.Log, 'Kolorowanie');
        InfoForm.Progress.Position:= 0;
        InfoForm.Progress.Step:= 1;
        InfoForm.Progress.Max:= 4;

        // 1/4
        blurred:= TBitmap.Create;
        blurred.Assign(blur);
        GBlur(blurred, 5);
        colored:= Colorize(blurred, cnv);
        blurred.Free;
        ImageForm.Image.Picture.Graphic:= colored;
        InfoForm.Progress.StepIt;

        // 2/4
        if composite > 1 then
        begin
          bluratr:= TBitmap.Create;
          bluratr.Assign(cnv);
          GBlur(bluratr, 2.5);
          LinearDodgeSelf(bluratr, colored);
          bluratr.Free;
          ImageForm.Image.Picture.Graphic:= colored;
        end;
        InfoForm.Progress.StepIt;

        // 3/4
        if composite > 2 then
        begin
          glow:= TBitmap.Create;
          glow.Assign(colored);
          GBlur(glow, 10);
          FadeSelf(glow, 0.7);
          LinearDodgeSelf(glow, colored);
          glow.Free;
          ImageForm.Image.Picture.Graphic:= colored;
        end;
        InfoForm.Progress.StepIt;

        // 4/4
        back:= TBitmap.Create;
        back.Assign(colored);
        GBlur(back, sqrt(w*h)*0.125);
        FadeSelf(back, 0.5);
        LinearDodgeSelf(back, colored);
        ImageForm.Image.Picture.Graphic:= colored;
        back.Free;
        colored.Free;
        InfoForm.Progress.StepIt;

        AddToLog(InfoForm.Log, 'Gotowe! Czas: '+FormatDateTime('hh:nn:ss', Now - timer));
        InfoForm.Caption:= 'Gotowe';
        InfoForm.Progress.Position:= 0;
      end
      else
      begin
        AddToLog(InfoForm.Log, 'Gotowe! Czas: '+FormatDateTime('hh:nn:ss', Now - timer));
        InfoForm.Caption:= 'Gotowe';
        InfoForm.Progress.Position:= 0;
      end;

      loop:= false;
    end;
  until (not loop) or Terminated;

  row:= nil;
  blur.Free;
  cnv.Free;
  {x.Free;
  y.Free;
  hxy.Free;}
end;

end.
