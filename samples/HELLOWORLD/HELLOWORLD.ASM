IOCB.EDTR = $00
PUTREC    = $09
BUFLEN    = $FF
CLS       = $7D
EOL       = $9B

RUNAD     = $02E0
ICCOM     = $0342
ICBADR    = $0344
ICBLEN    = $0348
CIO       = $E456

          *= $600
START     LDX #IOCB.EDTR*16
          LDA #PUTREC
          STA ICCOM,X
          LDA #<BUFFER
          STA ICBADR,X
          LDA #>BUFFER
          STA ICBADR+1,X
          LDA #<BUFLEN
          STA ICBLEN,X
          LDA #>BUFLEN
          STA ICBLEN+1,X
          JSR CIO
          RTS

BUFFER    .BYTE CLS,"HELLO, WORLD!",EOL

          *= RUNAD
          .WORD START
