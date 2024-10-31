unit info;

interface

uses
  Windows, Messages, SysUtils, Variants, Classes, Graphics, Controls, Forms,
  Dialogs, ComCtrls, StdCtrls;

type
  TInfoForm = class(TForm)
    Log: TListBox;
    Progress: TProgressBar;
    procedure FormCreate(Sender: TObject);
    procedure FormClose(Sender: TObject; var Action: TCloseAction);
  private
    { Private declarations }
  public
    running: Boolean;
  end;

var
  InfoForm: TInfoForm;

implementation

{$R *.dfm}

procedure TInfoForm.FormCreate(Sender: TObject);
begin
  running:= false;
end;

procedure TInfoForm.FormClose(Sender: TObject; var Action: TCloseAction);
begin
  // Uniemo¿liwienie wyl¹czenia informacji w trakcie generowania
  if running then Action:= caNone;
end;

end.
