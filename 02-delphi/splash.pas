unit splash;

interface

uses
  Windows, Messages, SysUtils, Variants, Classes, Graphics, Controls, Forms,
  Dialogs, jpeg, ExtCtrls;

type
  TSplashScreen = class(TForm)
    Image1: TImage;
    Timer1: TTimer;
    procedure Image1Click(Sender: TObject);
    procedure Timer1Timer(Sender: TObject);
    procedure FormClose(Sender: TObject; var Action: TCloseAction);
  private
    { Private declarations }
  public
    { Public declarations }
  end;

var
  SplashScreen: TSplashScreen;

implementation

{$R *.dfm}

procedure TSplashScreen.Image1Click(Sender: TObject);
begin
  Close;
end;

procedure TSplashScreen.Timer1Timer(Sender: TObject);
begin
  Timer1.Enabled:= False;
  Close;
end;

procedure TSplashScreen.FormClose(Sender: TObject;
  var Action: TCloseAction);
begin
  Free;
end;

end.
