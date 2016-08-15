; **********************************************************
; *                                                        *
; *                     ALYCLON Intro                      *
; *                                                        *
; *            Created by Lorenz Wiest MAY 1987            *
; *                                                        *
; **********************************************************

; LDA #A
; CMP #B
; A >= B -> CS
; A <  B -> CC

VDSLST       = $0200
SDMCTL       = $022F
SDLSTL       = $0230
GPRIOR       = $026F
STICK0       = $0278
STRIG0       = $0284
PCOLR0       = $02C0
PCOLR1       = $02C1
PCOLR2       = $02C2
COLOR3       = $02C7
CHRBAS       = $02F4

HPOSP0       = $D000                    ; GTIA hardware registers
HPOSP1       = $D001
HPOSP2       = $D002
HPOSM0       = $D004
HPOSM1       = $D005
HPOSM2       = $D006
HPOSM3       = $D007
COLPM0       = $D012
COLPM1       = $D013
COLPF0       = $D016
COLPF1       = $D017
COLPF2       = $D018
GRACTL       = $D01D

RANDOM       = $D20A                    ; POKEY hardware registers

DMACTL       = $D400                    ; ANTIC hardware registers
PMBASE       = $D407
WSYNC        = $D40A
NMIEN        = $D40E

SETVBV       = $E45C
XITVBV       = $E462

GRIDFORE.COL = $4B                      ; Bright purple
GRIDBACK.COL = $42                      ; Dark purple

REDLINE.COL  = $33                      ; Dark red

LOGO.COL0    = $1A                      ; Shades of yellow
LOGO.COL1    = $1F
LOGO.COL2    = $12
LOGO.COL3    = $14
LOGO.COL4    = $16
LOGO.COL5    = $18
LOGO.COL6    = $1A
LOGO.COL7    = $1C

SHIP.COL0    = $75                      ; Blue 
SHIP.COL1    = $0F                      ; White

JET.COL0     = $2A                      ; Yellow
JET.COL1     = $17                      ; Orange

MISS.COL     = $3C                      ; Bright red (missiles)

SHADOW.COL   = $00                      ; Black

SHIP.XINIT   = 123
SHIP.YMIN    = 163
SHIP.YMAX    = SHIP.YMIN+SHIP.LEN-1

JET.YMIN     = 183
JET.YMAX     = 225

SHADOW.DX    = 3
SHADOW.DY    = 4

STICK.RGHT   = $08
STICK.LEFT   = $04

L.PTR        = 203

             *= $3000
START
             JSR DLIST.INIT
             JSR DLI.INIT
             JSR CHAR.INIT
             JSR PM.INIT
             JSR VBI.INIT
             JSR SCR.INIT

; *** LOOP ************************************************

LOOP         JSR DELAY.1

             JSR MISSILE.CTRL
             LDA SHIP.X
             STA HPOSP0
             STA HPOSP1
             CLC
             ADC #SHADOW.DX
             STA HPOSP2

             LDA STICK0
             EOR #$FF
             TAY
             AND #STICK.RGHT
             BEQ LF
             INC SHIP.X                 ; Move right
             JMP LOOP

LF           TYA
             AND #STICK.LEFT
             BEQ LOOP
             DEC SHIP.X                 ; Move left
             JMP LOOP

; *** DISPLAY LIST ****************************************

DLIST.INIT
             LDA #REDLINE.COL
             STA COLOR3

             LDA #>SCRMEM               ; Clear 1K screen RAM
             LDX #4
             JSR CLR.PAGES

             LDA # <DSPLST
             STA SDLSTL
             LDA # >DSPLST
             STA SDLSTL+1
             RTS

BLK1         = $00                      ; ANTIC instructions
BLK8         = $70
DLI          = $80
LDM          = $40
JVB          = $41

GR0          = $02
GR1          = $06
GR12         = $04
GR14         = $0C

GRID.L00     = SCRMEM
GRID.L09     = SCRMEM+9*40

; Init DL LDM addresses for logo
;
; Logo memory layout 2 x 16 bytes/row (narrow PF)
; X = Intial DL LDM addresses
;
; Line 00 X...............################
; Line 01 ################X...............
; Line 02 X...............################
; Line 03 ################X...............
; ...
; Line 14 X...............################
; Line 15 ################X...............

LOGO.L00     = LOGOMEM
LOGO.L01     = LOGOMEM+1*$20+$10
LOGO.L02     = LOGOMEM+2*$20
LOGO.L03     = LOGOMEM+3*$20+$10
LOGO.L04     = LOGOMEM+4*$20
LOGO.L05     = LOGOMEM+5*$20+$10
LOGO.L06     = LOGOMEM+6*$20
LOGO.L07     = LOGOMEM+7*$20+$10
LOGO.L08     = LOGOMEM+8*$20
LOGO.L09     = LOGOMEM+9*$20+$10
LOGO.L10     = LOGOMEM+10*$20
LOGO.L11     = LOGOMEM+11*$20+$10
LOGO.L12     = LOGOMEM+12*$20
LOGO.L13     = LOGOMEM+13*$20+$10
LOGO.L14     = LOGOMEM+14*$20
LOGO.L15     = LOGOMEM+15*$20+$10

DSPLST
             .BYTE BLK8
             .BYTE BLK8
             .BYTE BLK8!DLI                         ; DLI #0
             .BYTE GR12!LDM,<GRID.L00,>GRID.L00
             .BYTE GR12
             .BYTE GR12
             .BYTE GR12
             .BYTE GR1!LDM,<TXTROW0,>TXTROW0
             .BYTE BLK8!DLI                         ; DLI #1
DS2          .BYTE GR14!LDM,<LOGO.L00,>LOGO.L00
DS3          .BYTE GR14!LDM!DLI,<LOGO.L01,>LOGO.L01 ; DLI #2
             .BYTE GR14!LDM,<LOGO.L02,>LOGO.L02
             .BYTE GR14!LDM!DLI,<LOGO.L03,>LOGO.L03 ; DLI #3
             .BYTE GR14!LDM,<LOGO.L04,>LOGO.L04
             .BYTE GR14!LDM!DLI,<LOGO.L05,>LOGO.L05 ; DLI #4
             .BYTE GR14!LDM,<LOGO.L06,>LOGO.L06
             .BYTE GR14!LDM!DLI,<LOGO.L07,>LOGO.L07 ; DLI #5
             .BYTE GR14!LDM,<LOGO.L08,>LOGO.L08
             .BYTE GR14!LDM!DLI,<LOGO.L09,>LOGO.L09 ; DLI #6
             .BYTE GR14!LDM,<LOGO.L10,>LOGO.L10
             .BYTE GR14!LDM!DLI,<LOGO.L11,>LOGO.L11 ; DLI #7
             .BYTE GR14!LDM,<LOGO.L12,>LOGO.L12
             .BYTE GR14!LDM!DLI,<LOGO.L13,>LOGO.L13 ; DLI #8
             .BYTE GR14!LDM,<LOGO.L14,>LOGO.L14
             .BYTE GR14!DLI!LDM,<LOGO.L15,>LOGO.L15 ; DLI #9
             .BYTE BLK8
             .BYTE GR1!LDM!DLI,<TXTROW1,>TXTROW1    ; DLI #10
             .BYTE BLK1
             .BYTE GR0!LDM,<TXTROW2,>TXTROW2
             .BYTE GR0
             .BYTE GR0!DLI                          ; DLI #11
             .BYTE BLK1
             .BYTE GR12!LDM,<GRID.L09,>GRID.L09
             .BYTE GR12
             .BYTE GR12!DLI                         ; DLI #12
             .BYTE GR12
             .BYTE GR12!DLI                         ; DLI #13
             .BYTE GR12
             .BYTE GR12
             .BYTE GR12
             .BYTE GR12
             .BYTE GR12
             .BYTE GR12
             .BYTE JVB, <DSPLST, >DSPLST

TXTROW0
             .BYTE "@@@@@@@@@@@@@@@@@@@@"
TXTROW1
             .BYTE "@@@@@@@@@@@@@@@@@@@@"
TXTROW2
             .BYTE "@@@@@@@@@@@@@@@@@@"
             .BYTE 'D-$20
             .BYTE 'e-$40
             .BYTE 'm-$40
             .BYTE 'o-$40
             .BYTE "@@@@@@@@@@@@@@@@@@"

             .BYTE "@@@@@@@@"
             .BYTE '1-$20
             .BYTE '9-$20
             .BYTE '8-$20
             .BYTE '7-$20
             .BYTE '@
             .BYTE '(-$20
             .BYTE 'C-$20
             .BYTE ')-$20
             .BYTE '@
             .BYTE 'b-$40
             .BYTE 'y-$40
             .BYTE '@
             .BYTE 'L-$20
             .BYTE 'o-$40
             .BYTE 'r-$40
             .BYTE 'e-$40
             .BYTE 'n-$40
             .BYTE 'z-$40
             .BYTE '@
             .BYTE 'W-$20
             .BYTE 'i-$40
             .BYTE 'e-$40
             .BYTE 's-$40
             .BYTE 't-$40
             .BYTE "@@@@@@@@"
             .BYTE "????????????????????????????????????????"

; *** DISPLAY LIST INTERRUPT ******************************

DLI.INIT
             LDA # <DLI.HANDLER
             STA VDSLST
             LDA # >DLI.HANDLER
             STA VDSLST+1
             LDA #192
             STA NMIEN
             RTS

DLI.HANDLER
             PHA
             TXA
             PHA
             LDA DLICOUNT
             STA WSYNC

             BEQ JP11

DL1          CMP #1
             BNE DL2
             LDA #$3D                   ; Narrow PF
             STA DMACTL
             BNE DLA

DL2          CMP #9
             BCC DLA

             CMP #9
             BNE DL10
             LDA #$3E                   ; Standard PF
             STA DMACTL
             BNE DLA

DL10         CMP #10
             BNE DL11
             LDA DLI.GR0.COL1
             STA COLPF1
             LDA DLI.GR0.COL2
             STA COLPF2
             JMP DLI.EXIT

DL11         CMP #11
             BNE DL12
JP11         LDA DLI.GR12.COL1
             STA COLPF1
             LDA DLI.GR12.COL2
             STA COLPF2
             JMP DLI.EXIT

DL12         CMP #12
             BNE DL13
             LDA #SHIP.COL0
             STA COLPM0
             LDA #SHIP.COL1
             STA COLPM1
             JMP DLI.EXIT

DL13         LDA #JET.COL0
             STA COLPM0
             LDA #JET.COL1
             STA COLPM1
             JMP DLI.EXIT

DLA          LDX DLICOUNT
             LDA LOGO.COLTAB-1,X
             STA COLPF0

DLI.EXIT
             INC DLICOUNT
             LDA DLICOUNT
             CMP #14
             BNE DL99
             LDA #0
             STA DLICOUNT
DL99         PLA
             TAX
             PLA
             RTI

DLICOUNT
             .BYTE 0

DLI.GR0.COL1
             .BYTE 0
DLI.GR0.COL2
             .BYTE 0

DLI.GR12.COL1
             .BYTE 0
DLI.GR12.COL2
             .BYTE 0

LOGO.COLTAB
             .BYTE LOGO.COL0
             .BYTE LOGO.COL1
             .BYTE LOGO.COL2
             .BYTE LOGO.COL3
             .BYTE LOGO.COL4
             .BYTE LOGO.COL5
             .BYTE LOGO.COL6
             .BYTE LOGO.COL7

; *** CHARACTER SET ***************************************

CHAR.INIT
             LDA #>CHARSET
             STA CHRBAS
             RTS

; *** PLAYER/MISSILES *************************************

PM.INIT
             LDA #>PM
             STA PMBASE

             LDX #8                     ; Clear 2K PM RAM
             JSR CLR.PAGES

             LDA #MISS.COL
             STA PCOLR0
             STA PCOLR1
             LDA #SHADOW.COL
             STA PCOLR2

             LDA #$3E
             STA SDMCTL
             LDA #$03
             STA GRACTL
             LDA #$01
             STA GPRIOR

             LDA #SHIP.XINIT
             STA SHIP.X
             STA HPOSP0
             STA HPOSP1
             LDA #SHIP.XINIT+SHADOW.DX
             STA HPOSP2

             LDX #0
             LDY #SHIP.YMIN
PM2          LDA PL0.DATA,X
             STA PL0MEM,Y
             LDA PL1.DATA,X
             STA PL1MEM,Y
             STA PL2MEM+SHADOW.DY,Y
             INX
             INY
             CPY #SHIP.YMAX+1
             BNE PM2

             LDY #JET.YMIN
             LDA #$18                   ; ...##...
PM3          STA PL0MEM,Y
             STA PL1MEM,Y
             INY
             CPY #JET.YMAX+1
             BNE PM3
             RTS

SHIP.LEN     = 14

PL0.DATA
             .BYTE $00                  ; ........
             .BYTE $00                  ; ........
             .BYTE $00                  ; ........
             .BYTE $00                  ; ........
             .BYTE $10                  ; ...#....
             .BYTE $10                  ; ...#....
             .BYTE $10                  ; ...#....
             .BYTE $00                  ; ........
             .BYTE $00                  ; ........
             .BYTE $28                  ; ..#.#...
             .BYTE $28                  ; ..#.#...
             .BYTE $6C                  ; .##.##..
             .BYTE $00                  ; ........
             .BYTE $00                  ; ........

PL1.DATA
             .BYTE $10                  ; ...#....
             .BYTE $10                  ; ...#....
             .BYTE $10                  ; ...#....
             .BYTE $38                  ; ..###...
             .BYTE $38                  ; ..###...
             .BYTE $38                  ; ..###...
             .BYTE $38                  ; ..###...
             .BYTE $38                  ; ..###...
             .BYTE $7C                  ; .#####..
             .BYTE $FE                  ; #######.
             .BYTE $FE                  ; #######.
             .BYTE $FE                  ; #######.
             .BYTE $7C                  ; .#####..
             .BYTE $28                  ; ..#.#...

MISS.LEN     = 15

MI0.DATA
             .BYTE $AA                  ; #.#.#.#.
             .BYTE $AA                  ; #.#.#.#.
             .BYTE $AA                  ; #.#.#.#.
             .BYTE $AA                  ; #.#.#.#.
             .BYTE $AA                  ; #.#.#.#.
             .BYTE $AA                  ; #.#.#.#.
             .BYTE $AA                  ; #.#.#.#.
             .BYTE $AA                  ; #.#.#.#.
             .BYTE $00                  ; ........
             .BYTE $AA                  ; #.#.#.#.
             .BYTE $AA                  ; #.#.#.#.
             .BYTE $00                  ; ........
             .BYTE $AA                  ; #.#.#.#.
             .BYTE $00                  ; ........
             .BYTE $AA                  ; #.#.#.#.

; *** VERTICAL BLANK INTERRUPT ****************************

VBI.INIT
             LDX #>VBI.HANDLER          ; Add to deferred VBI
             LDY #<VBI.HANDLER
             LDA #7
             JSR SETVBV
             RTS

VBI.HANDLER
             LDA CHARSET+7              ; Scroll 1st char in charset
             PHA
             LDY #6
VB1          LDA CHARSET,Y
             STA CHARSET+1,Y
             DEY
             BPL VB1
             PLA
             STA CHARSET

             LDY #JET.YMIN              ; Make jet sparkle
VB2          LDA RANDOM
             AND #$18                   ; ...##...
             STA PL0MEM,Y
             STA PL2MEM+SHADOW.DY,Y
             INY
             CPY #JET.YMAX+1
             BNE VB2
             JMP XITVBV

; *** SCREEN **********************************************

SCR.INIT
             JSR REDLINES.FADEIN
             JSR LOGO.SCROLLIN
             JSR TEXT.FADEIN
             JSR GRID.FADEIN
             RTS

HORI.BAR     = $CD

REDLINES.FADEIN
             LDX #0
             LDY #19
             LDA #HORI.BAR
LC1          STA TXTROW0,Y
             STA TXTROW1,X
             JSR DELAY.6
             INX
             DEY
             BPL LC1
             RTS

LOGO.SCROLLIN
             LDY #16                    ; # narrow PF bytes to scroll
FL2          LDX #0
FL3          INC DS2+1,X
             DEC DS3+1,X
             INX
             INX
             INX
             INX
             INX
             INX
             CPX #48
             BCC FL3
             JSR DELAY.12
             DEY
             BNE FL2
             JSR DELAY.250
             RTS

TEXT.FADEIN
             LDX #0
SC3          STX DLI.GR0.COL1
             JSR DELAY.6
             INX
             CPX #15
             BNE SC3
             JSR DELAY.250
             RTS

GRID.FADEIN
             LDA #GRIDBACK.COL&$F0
             STA DLI.GR12.COL1
             JSR DELAY.6

             LDX #0
SC6          INC DLI.GR12.COL1
             JSR DELAY.6
             INX
             CPX #GRIDFORE.COL-GRIDBACK.COL
             BNE SC6

             LDA #GRIDBACK.COL&$F0
             STA DLI.GR12.COL2
LC7          INC DLI.GR12.COL2
             INC DLI.GR12.COL1
             JSR DELAY.6
             LDA DLI.GR12.COL1
             CMP #GRIDFORE.COL
             BNE LC7
             RTS

; *** MISSILE CONTROL *************************************

MISS.RELDX   = 2
MISS.DX      = 2
MISS.YMIN    = 32                       ; first visible missile PL row on PF
MISS.YMAX    = 162                      ; last visible missile PL row on PF
MISS.VY      = 2

MISSILE.CTRL
             LDA MISS.Y
             BNE MISSILE.UPDATE

             LDA STRIG0
             BNE MISSILE.EXIT           ; trigger not pressed

MISSILE.INIT
             LDA SHIP.X
             CLC
             ADC #MISS.RELDX
             STA HPOSM0
             ADC #MISS.DX
             STA HPOSM1
             ADC #SHADOW.DX-MISS.DX
             STA HPOSM2
             ADC #MISS.DX
             STA HPOSM3

             LDA #MISS.YMAX
             STA MISS.Y
             JMP MISSILE.SET

MISSILE.UPDATE
MISSILE.CLR
             LDX MISS.Y
             LDY #MISS.YMAX+1
             JSR MISSILE.CLIPY
             BCC MISSILE.CLRSHADOW
             LDA #$0F                   ; ....####
             JSR MISSILE.CLRSHAPE

MISSILE.CLRSHADOW
             LDA MISS.Y
             CLC
             ADC #SHADOW.DY
             TAX
             LDY #MISS.YMAX+SHADOW.DY+1
             JSR MISSILE.CLIPY
             BCC MISSILE.MOVE
             LDA #$F0                   ; ####....
             JSR MISSILE.CLRSHAPE

MISSILE.MOVE
             SEC
             LDA MISS.Y
             SBC #MISS.VY
             STA MISS.Y

MISSILE.SET
             LDX MISS.Y
             LDY #MISS.YMAX+1
             JSR MISSILE.CLIPY
             BCC MISSILE.SETSHADOW
             LDA #$0F                   ; ....####
             JSR MISSILE.SETSHAPE

MISSILE.SETSHADOW
             LDA MISS.Y
             CLC
             ADC #SHADOW.DY
             TAX
             LDY #MISS.YMAX+SHADOW.DY+1
             JSR MISSILE.CLIPY
             BCC MISSILE.END
             LDA #$F0                   ; ####....
             JSR MISSILE.SETSHAPE
             RTS

MISSILE.END
             LDA #0
             STA MISS.Y
             STA HPOSM0
             STA HPOSM1
             STA HPOSM2
             STA HPOSM3

MISSILE.EXIT
             RTS

MISSILE.CLIPY
; IN
;   X = top row of missile PM on PF (may be outside PF)
;   Y = bottom row to clip PM against
; OUT
;   C = 0 -> missile PM offscreen
;   L.YTOP   = top row of missile PM on PF (may be outside PF)
;   L.YSTART = first visible row of missile PM on PF
;   L.YEND   = last visible row of missile PM on PF + 1

             STX L.YTOP
             STX L.YSTART

             DEX
             TXA
             CLC
             ADC #MISS.LEN
             STA L.YEND

             LDA L.YEND
             CMP #MISS.YMIN
             BCS SM1

             CLC                        ; missile outside PF
             RTS

SM1
             LDA L.YSTART
             CMP #MISS.YMIN
             BCS SM2

             LDA #MISS.YMIN             ; clip missile at top
             STA L.YSTART

SM2
             CPY L.YEND
             BEQ SM3
             BCC SM3

             SEC                        ; clip missile at bottom
             INC L.YEND
             RTS

SM3
             SEC
             STY L.YEND
             RTS

L.YTOP
             .BYTE 0
L.YSTART
             .BYTE 0
L.YEND
             .BYTE 0

MISSILE.CLRSHAPE
             EOR #$FF
             STA L.MASK
             LDX L.YSTART
LM1          LDA MI0MEM,X
             AND L.MASK
             STA MI0MEM,X
             INX
             CPX L.YEND
             BNE LM1
             RTS

MISSILE.SETSHAPE
             STA L.MASK
             LDX L.YSTART
             TXA
             SEC
             SBC L.YTOP
             TAY
LM2          LDA MI0.DATA,Y
             AND L.MASK
             STA L.FRAG
             LDA L.MASK
             EOR #$FF
             AND MI0MEM,X
             ORA L.FRAG
             STA MI0MEM,X
             INY
             INX
             CPX L.YEND
             BNE LM2
             RTS

L.MASK
             .BYTE 0
L.FRAG
             .BYTE 0
SHIP.X
             .BYTE 0
MISS.Y
             .BYTE 0

; *** UTILITIES *******************************************

DELAY.1
             PHA
             LDA #1
             BNE DY1
DELAY.6
             PHA
             LDA #6
             BNE DY1
DELAY.12
             PHA
             LDA #12
             BNE DY1
DELAY.250
             PHA
             LDA #250
DY1          STA SAVE.DELAY
             TXA
             PHA
             TYA
             PHA
DY2          LDY #4
             LDX #0
DY3          DEX
             BNE DY3
             DEY
             BNE DY3
             DEC SAVE.DELAY
             BNE DY2
             PLA
             TAY
             PLA
             TAX
             PLA
             RTS

SAVE.DELAY
             .BYTE 0

CLR.PAGES
; A = Start page to delete;
; X = # (>0) pages to delete

             STA L.PTR+1
             LDA #0
             STA L.PTR
             TAY
CP1          STA (L.PTR),Y
             INY
             BNE CP1
             INC L.PTR+1
             DEX
             BNE CP1
             RTS

             *= $4000
PM
             *= PM+$300
MI0MEM
             *= PM+$400
PL0MEM
             *= PM+$500
PL1MEM
             *= PM+$600
PL2MEM
             *= $4800
CHARSET                                 ; 64 x 8 = 512 ($200) bytes
             .BYTE $AA,$BF,$BF,$BF,$BF,$BF,$BF,$BF
             .BYTE $00,$18,$18,$18,$18,$00,$18,$00
             .BYTE $00,$66,$66,$66,$00,$00,$00,$00
             .BYTE $00,$66,$FF,$66,$66,$FF,$66,$00
             .BYTE $6C,$00,$7E,$06,$7E,$66,$7E,$00
             .BYTE $6C,$00,$7C,$C6,$C6,$C6,$7C,$00
             .BYTE $6C,$00,$C6,$C6,$C6,$C6,$7E,$00
             .BYTE $7C,$C6,$C6,$DC,$C6,$C6,$FC,$C0
             .BYTE $0E,$1C,$18,$18,$18,$1C,$0E,$00
             .BYTE $70,$38,$18,$18,$18,$38,$70,$00
             .BYTE $00,$66,$3C,$FF,$3C,$66,$00,$00
             .BYTE $00,$18,$18,$7E,$18,$18,$00,$00
             .BYTE $00,$00,$00,$00,$00,$18,$18,$30
             .BYTE $00,$00,$00,$FF,$FF,$00,$00,$00
             .BYTE $00,$00,$00,$00,$00,$18,$18,$00
             .BYTE $00,$06,$0C,$18,$30,$60,$40,$00

             .BYTE $00,$7C,$C6,$E6,$D6,$CE,$C6,$7C
             .BYTE $00,$18,$38,$78,$18,$18,$18,$FE
             .BYTE $00,$7C,$C6,$06,$7C,$C0,$C6,$FE
             .BYTE $00,$FE,$06,$1C,$0C,$C6,$66,$3C
             .BYTE $00,$0C,$1C,$3C,$6C,$FE,$0C,$0C
             .BYTE $00,$FE,$C6,$C0,$FC,$06,$C6,$7C
             .BYTE $00,$3E,$60,$C0,$FC,$C6,$C6,$7C
             .BYTE $00,$FE,$06,$0C,$18,$30,$60,$60
             .BYTE $00,$7C,$C6,$C6,$7C,$C6,$C6,$7C
             .BYTE $00,$7C,$C6,$C6,$7E,$06,$0C,$78
             .BYTE $00,$00,$18,$18,$00,$18,$18,$00
             .BYTE $00,$00,$18,$18,$00,$18,$18,$30
             .BYTE $06,$0C,$18,$30,$18,$0C,$06,$00
             .BYTE $00,$00,$7E,$00,$00,$7E,$00,$00
             .BYTE $60,$30,$18,$0C,$18,$30,$60,$00
             .BYTE $00,$3C,$66,$06,$0C,$18,$00,$18

             .BYTE $00,$00,$00,$00,$00,$00,$00,$00
             .BYTE $00,$00,$7E,$06,$7E,$66,$7E,$00
             .BYTE $C0,$C0,$C0,$FC,$C6,$C6,$FC,$00
             .BYTE $7C,$C6,$C0,$C0,$C0,$C6,$7C,$00
             .BYTE $F8,$CC,$C6,$C6,$C6,$CC,$F8,$00
             .BYTE $00,$00,$7C,$C6,$FE,$C0,$7E,$00
             .BYTE $1E,$30,$FE,$30,$30,$30,$30,$00
             .BYTE $00,$00,$7E,$C6,$C6,$7E,$06,$FC
             .BYTE $C0,$C0,$C0,$FC,$C6,$C6,$C6,$00
             .BYTE $18,$00,$38,$18,$18,$18,$3C,$00
             .BYTE $06,$00,$06,$06,$06,$06,$C6,$7C
             .BYTE $C0,$CE,$D8,$F0,$D8,$CC,$C6,$00
             .BYTE $60,$60,$60,$60,$60,$60,$7E,$00
             .BYTE $00,$00,$EE,$FE,$D6,$D6,$C6,$00
             .BYTE $00,$00,$FC,$C6,$C6,$C6,$C6,$00
             .BYTE $00,$00,$7C,$C6,$C6,$C6,$7C,$00

             .BYTE $00,$00,$FC,$C6,$C6,$FC,$C0,$C0
             .BYTE $00,$00,$7E,$C6,$C6,$7E,$06,$06
             .BYTE $00,$00,$FC,$C6,$C0,$C0,$C0,$00
             .BYTE $00,$00,$7E,$C0,$FE,$06,$FC,$00
             .BYTE $30,$30,$FE,$30,$30,$30,$1E,$00
             .BYTE $00,$00,$C6,$C6,$C6,$C6,$7E,$00
             .BYTE $00,$00,$C6,$C6,$C6,$6C,$38,$00
             .BYTE $C6,$C6,$D6,$D6,$D6,$FE,$C6,$00
             .BYTE $00,$00,$C6,$6C,$38,$6C,$C6,$00
             .BYTE $00,$00,$C6,$C6,$C6,$7E,$06,$FC
             .BYTE $00,$00,$FE,$1C,$38,$70,$FE,$00
             .BYTE $00,$1E,$18,$18,$18,$18,$1E,$00
             .BYTE $00,$40,$60,$30,$18,$0C,$06,$00
             .BYTE $00,$78,$18,$18,$18,$18,$78,$00
             .BYTE $00,$08,$1C,$36,$63,$00,$00,$00
             .BYTE $00,$00,$00,$00,$FF,$00,$00,$00

LOGOMEM                                 ; 32 x 16 = 512 ($200) bytes
             .BYTE $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
             .BYTE $00,$18,$00,$FC,$00,$1F,$9F,$F0,$3F,$FF,$00,$0F,$C0,$F8,$1F,$00
             .BYTE $00,$3C,$00,$78,$00,$70,$F9,$E0,$0C,$3C,$00,$38,$70,$7C,$0E,$00
             .BYTE $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
             .BYTE $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
             .BYTE $00,$3C,$00,$78,$00,$E0,$38,$F0,$18,$3C,$00,$70,$38,$7C,$0E,$00
             .BYTE $00,$7E,$00,$78,$01,$C0,$18,$78,$30,$3C,$00,$E0,$1C,$76,$0E,$00
             .BYTE $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
             .BYTE $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
             .BYTE $00,$7E,$00,$78,$01,$C0,$18,$3C,$60,$3C,$00,$E0,$1C,$76,$0E,$00
             .BYTE $00,$DF,$00,$78,$03,$C0,$18,$1E,$C0,$3C,$01,$E0,$1E,$73,$0E,$00
             .BYTE $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
             .BYTE $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
             .BYTE $00,$CF,$00,$78,$03,$C0,$00,$0F,$80,$3C,$01,$E0,$1E,$73,$0E,$00
             .BYTE $01,$8F,$80,$78,$03,$C0,$00,$07,$80,$3C,$01,$E0,$1E,$71,$8E,$00
             .BYTE $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
             .BYTE $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
             .BYTE $01,$87,$80,$78,$03,$C0,$00,$07,$80,$3C,$01,$E0,$1E,$71,$8E,$00
             .BYTE $03,$07,$C0,$78,$03,$C0,$00,$07,$80,$3C,$01,$E0,$1E,$70,$CE,$00
             .BYTE $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
             .BYTE $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
             .BYTE $03,$03,$C0,$78,$03,$C0,$00,$07,$80,$3C,$01,$E0,$1E,$70,$CE,$00
             .BYTE $07,$FF,$E0,$78,$01,$C0,$18,$07,$80,$3C,$00,$E0,$1C,$70,$6E,$00
             .BYTE $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
             .BYTE $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
             .BYTE $06,$01,$E0,$78,$09,$C0,$18,$07,$80,$3C,$04,$E0,$1C,$70,$6E,$00
             .BYTE $0C,$01,$F0,$78,$18,$E0,$30,$07,$80,$3C,$0C,$70,$38,$70,$3E,$00
             .BYTE $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
             .BYTE $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00
             .BYTE $1C,$00,$F0,$78,$38,$70,$E0,$0F,$C0,$3C,$1C,$38,$70,$70,$3E,$00
             .BYTE $3E,$01,$FC,$FF,$F8,$1F,$80,$1F,$E0,$7F,$FC,$0F,$C0,$F8,$1E,$00
             .BYTE $00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00,$00

SCRMEM                                  ; 40 x 24 = 960 ($3C0) bytes

             *= $2E0
             .WORD START
