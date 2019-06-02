; **********************************************************
; *                                                        *
; *        Recreates the ACTIVISION scrolling banner       *
; *                                                        *
; *           Created by Lorenz Wiest 07-JUN-2016          *
; *                                                        *
; **********************************************************

; LW 13-JUN-2016 Cleanup
; LW 12-JUN-2016 Correct PM Banner syncing, Code Complete
; LW 11-JUN-2016 Clean non-DMA PM Banner
; LW 10-JUN-2016 Two-color text
; LW 09-JUN-2016 Wide Playfield, Finescrolling, Custom Font, Background Color DLIs
; LW 07-JUN-2016 Created

COL.BCKG.TOP    = $88    ; Colors
COL.BCKG.BOT    = $C4
COL.FONT.NRML   = $0C
COL.FONT.EMPH   = $1C
COL.BANNER1     = $44
COL.BANNER2     = $26
COL.BANNER3     = $26
COL.BANNER4     = $1A
COL.BANNER5     = $D6
COL.BANNER6     = $D6
COL.BANNER7     = $84

BANNERHPOS.MIN  = 0
BANNERHPOS.INIT = 173
BANNERHPOS.MAX  = 237

DLI             = $80    ; Display List instructions
LDM             = $40
HSCR            = $10
BLK1            = $00
BLK7            = $60
BLK8            = $70
GR1             = $06
JVB             = $41

VDSLST          = $200
SDLLST          = $230

HPOSP0          = $D000  ; GTIA hardware registers
HPOSM0          = $D004
SIZEP0          = $D008
SIZEM           = $D00C
GRAFP0          = $D00D
GRAFM           = $D011
COLPM0          = $D012
COLPF0          = $D016
COLPF1          = $D017
COLBK           = $D01A
PRIOR           = $D01B
GRACTL          = $D01D

DMACTL          = $D400  ; ANTIC hardware registers
HSCROL          = $D404
CHBASE          = $D409
WSYNC           = $D40A
VBCOUNT         = $D40B
NMIEN           = $D40E

SETVBV          = $E45C
XITVBV          = $E462

    *= $3000

DLISTTAB
    .BYTE BLK1!DLI
    .BYTE BLK7
    .BYTE BLK8
    .BYTE BLK8
    .BYTE BLK8
    .BYTE BLK8
    .BYTE BLK8
    .BYTE BLK8
    .BYTE BLK8
    .BYTE BLK8
    .BYTE BLK8
    .BYTE BLK8
    .BYTE BLK8
    .BYTE BLK8!DLI
    .BYTE BLK8
    .BYTE BLK8
    .BYTE BLK8
    .BYTE BLK8
    .BYTE BLK8
    .BYTE BLK8
    .BYTE BLK8
    .BYTE BLK8
    .BYTE BLK8
    .BYTE BLK8
    .BYTE BLK8
    .BYTE BLK8
    .BYTE BLK8!DLI
    .BYTE BLK1
LDMPTR.TMP
LDMPTR          = LDMPTR.TMP!1
    .BYTE GR1!LDM!HSCR, <LINEINIT, >LINEINIT
    .BYTE JVB, <DLISTTAB, >DLISTTAB

START
    LDA #<DLISTTAB
    STA SDLLST
    LDA #>DLISTTAB
    STA SDLLST+1

; *** DLI ***

    LDA #<DLIHANDLER
    STA VDSLST
    LDA #>DLIHANDLER
    STA VDSLST+1
    LDA #192
    STA NMIEN

;*** VBI ****

    LDX #>VBIHANDLER     ; Add to deferred VBI
    LDY #<VBIHANDLER
    LDA #7
    JSR SETVBV

;*** END ***

FOREVER
    JMP FOREVER

;*** VBI HANDLER ***

VBIHANDLER
    DEC SHSCROL
    BPL VBI.HSCROL

    LDA #7
    STA SHSCROL

    INC LDMPTR
    BNE VBI.SKIP1
    INC LDMPTR+1
VBI.SKIP1

    LDA LDMPTR+1         ; If (LDMPTR) <= LINESWITCH VBI.HSCROL
    CMP #>LINESWITCH
    BEQ VBI.SKIP2
    BCC VBI.CHKBANNERON
    BCS VBI.INITLINE
VBI.SKIP2
    LDA LDMPTR
    CMP #<LINESWITCH
    BEQ VBI.INITLINE
    BCC VBI.CHKBANNERON

VBI.INITLINE
    LDA #<LINESTART
    STA LDMPTR
    LDA #>LINESTART
    STA LDMPTR+1

VBI.CHKBANNERON
    LDA LDMPTR
    CMP #<BANNERSTART-24
    BNE VBI.CHKBANNEROFF

    LDA #BANNERHPOS.MAX
    STA BANNERHPOS
    JMP VBI.HSCROL

VBI.CHKBANNEROFF
    LDA LDMPTR
    CMP #<BANNEREND
    BNE VBI.HSCROL

    LDA #BANNERHPOS.MIN
    STA BANNERHPOS

VBI.HSCROL
    LDA SHSCROL
    STA HSCROL

    LDA BANNERHPOS
    BEQ VBI.EXIT
    DEC BANNERHPOS

VBI.EXIT
    JMP XITVBV

SHSCROL
    .BYTE 0

BANNERHPOS
    .BYTE BANNERHPOS.INIT

LINESTART
    .BYTE ' -$20
    .BYTE ';+$20
    .BYTE 'N+$20
    .BYTE 'N+$20
    .BYTE 'E+$20
    .BYTE 'R+$20
    .BYTE ' -$20
    .BYTE 'R-$20
    .BYTE 'E-$20
    .BYTE 'C-$20
    .BYTE 'R-$20
    .BYTE 'E-$20
    .BYTE 'A-$20
    .BYTE 'T-$20
    .BYTE 'E-$20
    .BYTE 'D-$20
    .BYTE ' -$20
    .BYTE 'B-$20
    .BYTE 'Y-$20
    .BYTE ' -$20
    .BYTE '=+$20
    .BYTE 'O+$20
    .BYTE 'R+$20
    .BYTE 'E+$20
    .BYTE 'N+$20
    .BYTE 'Z+$20
    .BYTE ' -$20
    .BYTE '?+$20
    .BYTE 'I+$20
    .BYTE 'E+$20
    .BYTE 'S+$20
    .BYTE 'T+$20
    .BYTE ' -$20
    .BYTE '2-$20
    .BYTE '0-$20
    .BYTE '1-$20
    .BYTE '6-$20
LINEINIT                 ; <- Init Position
    .BYTE ' -$20
    .BYTE ' -$20
    .BYTE ' -$20
    .BYTE ' -$20
    .BYTE ' -$20
    .BYTE ' -$20
    .BYTE ' -$20
    .BYTE ' -$20
    .BYTE ' -$20
    .BYTE ' -$20
    .BYTE ' -$20
    .BYTE ' -$20
    .BYTE ' -$20
    .BYTE ' -$20
    .BYTE '>-$20
    .BYTE 'H-$20
    .BYTE 'E-$20
BANNERSTART
    .BYTE ' -$20
    .BYTE ' -$20
    .BYTE ' -$20
    .BYTE '[-$20
BANNEREND
    .BYTE '\-$20
    .BYTE ']-$20
    .BYTE '^-$20
    .BYTE '_-$20
    .BYTE ' -$20
    .BYTE '<+$20
    .BYTE 'C+$20
    .BYTE 'R+$20
    .BYTE 'O+$20
    .BYTE 'L+$20
    .BYTE 'L+$20
    .BYTE 'I+$20
    .BYTE 'N+$20
    .BYTE 'G+$20
    .BYTE ' -$20         ; <-- Switch position
    .BYTE ';+$20
    .BYTE 'A+$20
    .BYTE 'N+$20
    .BYTE 'N+$20
    .BYTE 'E+$20
    .BYTE 'R+$20
    .BYTE ' -$20
    .BYTE 'R-$20
    .BYTE 'E-$20
    .BYTE 'C-$20
    .BYTE 'R-$20
    .BYTE 'E-$20
    .BYTE 'A-$20
    .BYTE 'T-$20
    .BYTE 'E-$20
    .BYTE 'D-$20
    .BYTE ' -$20
    .BYTE 'B-$20
    .BYTE 'Y-$20
    .BYTE ' -$20
    .BYTE '=+$20

LINEEND
LINESWITCH      = LINEEND-24+3

;*** DLI HANDLER ***

DLIHANDLER
    PHA
    TXA
    PHA
    TYA
    PHA

    LDA VBCOUNT
    STA WSYNC
    CMP #10
    BCS DLI.SKIP1

    LDA #COL.BCKG.TOP
    STA COLBK
    JMP DLI.EXIT

DLI.SKIP1
    CMP #80
    BCS DLI.SKIP2

    LDA #COL.BCKG.BOT
    STA COLBK
    JMP DLI.EXIT

DLI.SKIP2
    LDA #COL.BCKG.BOT    ; Hack: Hide Non-DMA PM jitter
    STA COLPM0

    LDA #$00             ; B2..1: PM off
    STA GRACTL

    LDA #$33             ; B5: DL DMA on, B4: PM single res on, B3..2: PM DMA off, B1..0: Wide display (24 GR1 chars)
    STA DMACTL

    LDA #$03             ; B2..1: P0 size x 4
    STA SIZEP0
    LDA #$03             ; B2..1: M0 size x 4
    STA SIZEM
    LDA #$04             ; PF > PL > BACK
    STA PRIOR

    LDA #COL.FONT.NRML
    STA COLPF0
    LDA #COL.FONT.EMPH
    STA COLPF1

    LDA #>CHARSET
    STA CHBASE

    LDA BANNERHPOS
    STA HPOSP0
    CLC
    ADC #22
    TAY

    STA WSYNC            ; Makes banner colors skip the first scan line of PM0
    LDX #6
LOOP.COL2
    LDA DLICOLTAB,X
    STA WSYNC
    STA COLPM0
    STY HPOSM0

    LDA #$FC             ; P0 bit pattern ######..
    STA GRAFP0
    LDA #$03             ; Mx bit pattern ......##
    STA GRAFM

    DEY
    DEX
    BPL LOOP.COL2

    LDA #$03             ; B2..1: PM on
    STA WSYNC            ; !
    STA GRACTL

    LDA #$3F             ; B5: DL DMA on, B4: PM single res on, B3..2: PM DMA on, B1..0: Wide display (24 GR1 chars)
    STA DMACTL

    LDA #0
    STA HPOSP0
    STA HPOSM0

DLI.EXIT
    PLA
    TAY
    PLA
    TAX
    PLA
    RTI

DLICOLTAB
    .BYTE COL.BANNER7
    .BYTE COL.BANNER6
    .BYTE COL.BANNER5
    .BYTE COL.BANNER4
    .BYTE COL.BANNER3
    .BYTE COL.BANNER2
    .BYTE COL.BANNER1

    *= $3200

; ROM Character Set
; --------------------------------
;  !"#$%&'()*+,-./0123456789:;<=>?
; @ABCDEFGHIJKLMNOPQRSTUVWXYZ[\]^_
; ********************************
; *abcdefghijklmnopqrstuvwxyz*****
; --------------------------------
;
; Custom Character Set
; --------------------------------
;  !"#$%&'()*+,-./0123456789:SDLTW 
; @abcdefghijklmnopqrstuvwxyz@@@@@
; --------------------------------

CHARSET
    .BYTE $00,$00,$00,$00,$00,$00,$00,$00
    .BYTE $00,$18,$18,$18,$18,$00,$18,$00
    .BYTE $00,$66,$66,$66,$00,$00,$00,$00
    .BYTE $00,$66,$FF,$66,$66,$FF,$66,$00
    .BYTE $18,$3E,$60,$3C,$06,$7C,$18,$00
    .BYTE $00,$66,$6C,$18,$30,$66,$46,$00
    .BYTE $1C,$36,$1C,$38,$6F,$66,$3B,$00
    .BYTE $00,$18,$18,$18,$00,$00,$00,$00
    .BYTE $00,$0E,$1C,$18,$18,$1C,$0E,$00
    .BYTE $00,$70,$38,$18,$18,$38,$70,$00
    .BYTE $00,$66,$3C,$FF,$3C,$66,$00,$00
    .BYTE $00,$18,$18,$7E,$18,$18,$00,$00
    .BYTE $00,$00,$00,$00,$00,$18,$18,$30
    .BYTE $00,$00,$00,$7E,$00,$00,$00,$00
    .BYTE $00,$00,$00,$00,$00,$18,$18,$00
    .BYTE $00,$06,$0C,$18,$30,$60,$40,$00

    .BYTE $00,$3C,$66,$6E,$76,$66,$3C,$00
    .BYTE $00,$18,$38,$18,$18,$18,$7E,$00
    .BYTE $00,$3C,$66,$0C,$18,$30,$7E,$00
    .BYTE $00,$7E,$0C,$18,$0C,$66,$3C,$00
    .BYTE $00,$0C,$1C,$3C,$6C,$7E,$0C,$00
    .BYTE $00,$7E,$60,$7C,$06,$66,$3C,$00
    .BYTE $00,$3C,$60,$7C,$66,$66,$3C,$00
    .BYTE $00,$7E,$06,$0C,$18,$30,$30,$00
    .BYTE $00,$3C,$66,$3C,$66,$66,$3C,$00
    .BYTE $00,$3C,$66,$3E,$06,$0C,$38,$00
    .BYTE $00,$00,$18,$18,$00,$18,$18,$00
    .BYTE $00,$7C,$66,$7C,$66,$66,$7C,$00
    .BYTE $00,$3C,$60,$3C,$06,$06,$3C,$00
    .BYTE $00,$60,$60,$60,$60,$60,$7E,$00
    .BYTE $00,$7E,$18,$18,$18,$18,$18,$00
    .BYTE $00,$63,$63,$6B,$7F,$77,$63,$00

    .BYTE $00,$18,$3C,$7E,$7E,$3C,$18,$00
    .BYTE $00,$00,$3C,$06,$3E,$66,$3E,$00
    .BYTE $00,$60,$60,$7C,$66,$66,$7C,$00
    .BYTE $00,$00,$3C,$60,$60,$60,$3C,$00
    .BYTE $00,$06,$06,$3E,$66,$66,$3E,$00
    .BYTE $00,$00,$3C,$66,$7E,$60,$3C,$00
    .BYTE $00,$0E,$18,$3E,$18,$18,$18,$00
    .BYTE $00,$00,$3E,$66,$66,$3E,$06,$7C
    .BYTE $00,$60,$60,$7C,$66,$66,$66,$00
    .BYTE $00,$18,$00,$38,$18,$18,$3C,$00
    .BYTE $00,$06,$00,$06,$06,$06,$06,$3C
    .BYTE $00,$60,$60,$6C,$78,$6C,$66,$00
    .BYTE $00,$38,$18,$18,$18,$18,$3C,$00
    .BYTE $00,$00,$66,$7F,$7F,$6B,$63,$00
    .BYTE $00,$00,$7C,$66,$66,$66,$66,$00
    .BYTE $00,$00,$3C,$66,$66,$66,$3C,$00

    .BYTE $00,$00,$7C,$66,$66,$7C,$60,$60
    .BYTE $00,$00,$3E,$66,$66,$3E,$06,$06
    .BYTE $00,$00,$7C,$66,$60,$60,$60,$00
    .BYTE $00,$00,$3E,$60,$3C,$06,$7C,$00
    .BYTE $00,$18,$7E,$18,$18,$18,$0E,$00
    .BYTE $00,$00,$66,$66,$66,$66,$3E,$00
    .BYTE $00,$00,$66,$66,$66,$3C,$18,$00
    .BYTE $00,$00,$63,$6B,$7F,$3E,$36,$00
    .BYTE $00,$00,$66,$3C,$18,$3C,$66,$00
    .BYTE $00,$00,$66,$66,$66,$3E,$0C,$78
    .BYTE $00,$00,$7E,$0C,$18,$30,$7E,$00
    .BYTE $00,$00,$00,$00,$01,$03,$06,$0C
    .BYTE $00,$2F,$61,$ED,$A9,$E9,$29,$2D
    .BYTE $00,$F0,$11,$53,$56,$5C,$58,$50
    .BYTE $00,$FE,$80,$3A,$A2,$BA,$8A,$BA
    .BYTE $00,$00,$00,$E9,$AD,$AF,$AB,$E9

    *= $02E0
    .WORD START
