object ImageForm: TImageForm
  Left = 216
  Top = 353
  BorderStyle = bsDialog
  Caption = 'Atraktor'
  ClientHeight = 512
  ClientWidth = 512
  Color = clBlack
  Font.Charset = DEFAULT_CHARSET
  Font.Color = clWindowText
  Font.Height = -11
  Font.Name = 'Tahoma'
  Font.Style = []
  OldCreateOrder = False
  Position = poDesktopCenter
  OnClose = FormClose
  OnCreate = FormCreate
  PixelsPerInch = 96
  TextHeight = 13
  object Image: TImage
    Left = 0
    Top = 0
    Width = 512
    Height = 512
    Align = alClient
    Center = True
  end
  object SaveDialog: TSavePictureDialog
    DefaultExt = 'jpg'
    Filter = 
      'All (*.jpg;*.jpeg;*.bmp)|*.jpg;*.jpeg;*.bmp|Bitmaps (*.bmp)|*.bm' +
      'p|JPEG Image File (*.jpg;*.jpeg)|*.jpg;*.jpeg'
    FilterIndex = 3
    Options = [ofOverwritePrompt, ofHideReadOnly, ofEnableSizing]
    Title = 'Zapisz atraktor...'
    Left = 480
    Top = 8
  end
end
