object InfoForm: TInfoForm
  Left = 216
  Top = 906
  Width = 512
  Height = 170
  BorderStyle = bsSizeToolWin
  Caption = 'Generowanie atraktora...'
  Color = clBtnFace
  Font.Charset = DEFAULT_CHARSET
  Font.Color = clWindowText
  Font.Height = -11
  Font.Name = 'MS Sans Serif'
  Font.Style = []
  FormStyle = fsStayOnTop
  OldCreateOrder = False
  Position = poDefaultPosOnly
  OnClose = FormClose
  OnCreate = FormCreate
  PixelsPerInch = 96
  TextHeight = 13
  object Log: TListBox
    Left = 0
    Top = 0
    Width = 504
    Height = 114
    Align = alClient
    ItemHeight = 13
    TabOrder = 0
  end
  object Progress: TProgressBar
    Left = 0
    Top = 114
    Width = 504
    Height = 22
    Align = alBottom
    TabOrder = 1
  end
end
