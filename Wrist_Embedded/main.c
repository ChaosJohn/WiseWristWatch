/*---------------------------------------------------------------------------------------------------------*/
/*                                                                                                         */
/* Copyright (c) Nuvoton Technology Corp. All rights reserved.                                             */
/*                                                                                                         */
/*---------------------------------------------------------------------------------------------------------*/

#include <stdio.h>
#include <string.h>
#include <math.h> 
#include "M051Series.h"
#include "LCD_Driver.h"
#include "EEPROM_24LC64.h"

#define PLLCON_SETTING      SYSCLK_PLLCON_50MHz_XTAL
#define PLL_CLOCK           50000000

#define _I2C_WRITE_OR(port, u8Data) ((port)->I2CDAT |= (u8Data));((port)->I2CON = I2C_I2CON_ENS1_Msk | I2C_I2CON_SI_Msk)

#define MMA_READ_CONTROL      0x3B /* Address of slave for read  */
#define MMA_WRITE_CONTROL     0x3A /* Address of slave for write */
//#define MAC_ADDR 0xFA
#define MAC_ADDR 0x00
#define MAC0 0x00
#define MAC1 0x04
#define MAC2 0xA3


void _I2C_WAIT_READY_TIMEOUT(I2C_T *port) {
	extern uint32_t CyclesPerUs;
	SysTick->LOAD = 2000 * CyclesPerUs;
	SysTick->VAL  =  (0x00);
	SysTick->CTRL = SysTick_CTRL_CLKSOURCE_Msk | SysTick_CTRL_ENABLE_Msk;

	while(((port)->I2CON & I2C_I2CON_SI_Msk) == 0
		&& (SysTick->CTRL & SysTick_CTRL_COUNTFLAG_Msk) == 0);
	
}

void Delay(int us) {
	extern uint32_t CyclesPerUs;
	SysTick->LOAD = us * CyclesPerUs;
	SysTick->VAL  =  (0x00);
	SysTick->CTRL = SysTick_CTRL_CLKSOURCE_Msk | SysTick_CTRL_ENABLE_Msk;

	while((SysTick->CTRL & SysTick_CTRL_COUNTFLAG_Msk) == 0);	
}




void I2C_Init(void)
{
	SYS->IPRSTC2 |= SYS_IPRSTC2_I2C_RST_Msk; //C: Peripheral Reset Control Resister 2: I2C-bit = 1 => controller reset
	SYS->IPRSTC2 &= ~SYS_IPRSTC2_I2C_RST_Msk;//C: Peripheral Reset Control Resister 2: I2C-bit = 0 => set controller to normal mode
	
  I2C0->I2CLK = I2C_I2CLK_DIV4(120); /* 48000000Hz / 4 / 120 = 100000Hz */
	_I2C_ENABLE_TIMEOUT_COUNTER(I2C0);
}


uint8_t EEPROM_Read(uint32_t u32Addr)
{
	int try_count = 0;
    int32_t i32Err;
    uint8_t u8Data;

    u8Data = 0;
    do 
    {
        i32Err = 0;
        /* Send start */
        _I2C_START(I2C0);
        _I2C_WAIT_READY_TIMEOUT(I2C0);
        /* Send control byte*/
        _I2C_WRITE(I2C0, MMA_WRITE_CONTROL); //C: [EEPROM_WRITE_ADDR = A0H = 10100000B] => [slave addr = 1010000B] + [r/w = 0] => [control = write]
        _I2C_WAIT_READY_TIMEOUT(I2C0);
        if(I2C0->I2CSTATUS == 0x18){ 
            /* ACK */
            _I2C_WRITE(I2C0, u32Addr & 0xFFUL); 			
            _I2C_WAIT_READY_TIMEOUT(I2C0);
						//printf("I2C0->I2CSTATUS = %x\n", I2C0->I2CSTATUS);
						if(I2C0->I2CSTATUS == 0x28){
								/* ACK */
								/* Send data */
								_I2C_START(I2C0); // repeat start
								_I2C_WAIT_READY_TIMEOUT(I2C0);
								//printf("I2C0->I2CSTATUS = %x\n", I2C0->I2CSTATUS);
								if(I2C0->I2CSTATUS == 0x10 || I2C0->I2CSTATUS == 0x08){
										/* ACK */
										/* Send control byte */
										_I2C_WRITE(I2C0, MMA_READ_CONTROL);
										_I2C_WAIT_READY_TIMEOUT(I2C0);												
										if(I2C0->I2CSTATUS == 0x40){
												/* Read data */
												u8Data = _I2C_READ_NAK(I2C0);
												//printf("I2C0->I2CSTATUS = %x\n", I2C0->I2CSTATUS);
												if(I2C0->I2CSTATUS == 0x58){
														/* NACK */
														/* Send stop */ 
														_I2C_STOP(I2C0);
												}else{
														/* ACK */
														/* read data error */
														i32Err = 5;
												}
										}else{
												/* NACK */
												/* Send control read error */
												i32Err = 4;
										}
								}else{
										/* NACK */
										/* Send start error */
										i32Err = 3;
								}
						}else{
								/* NACK */						
								/* Send low address error */
								i32Err = 2;
						}            
        }else{
            /* NACK */            
            /* Send control write error */
            i32Err = 1;
        }

        if(i32Err){
            /* Send stop */
            _I2C_STOP(I2C0);
						printf("err %d\t", i32Err);
            SYS_SysTickDelay(100);
						//printf("err\n");
        }

    }while(i32Err && ++try_count < 2);

    return (i32Err == 0 ? u8Data : 0xFF);
}

void i2cWrite(uint32_t u32Addr, uint8_t u8Data) {
	uint32_t try_count = 0;
	int32_t i32Err;
	
	do {
			i32Err = 0;
			/* Send start */
			_I2C_START(I2C0);
			_I2C_WAIT_READY_TIMEOUT(I2C0);
			/* Send control byte*/
			_I2C_WRITE(I2C0, MMA_WRITE_CONTROL); //C: [EEPROM_WRITE_ADDR = A0H = 10100000B] => [slave addr = 1010000B] + [r/w = 0] => [control = write]
			_I2C_WAIT_READY_TIMEOUT(I2C0);
			if(I2C0->I2CSTATUS == 0x18){ 
					/* ACK */
					/* Send operation address byte */ 
					_I2C_WRITE(I2C0, u32Addr & 0xFFUL); 			
					_I2C_WAIT_READY_TIMEOUT(I2C0);
					if(I2C0->I2CSTATUS == 0x28){
							/* ACK */
							/* Send data */
							_I2C_WRITE(I2C0, u8Data);
							_I2C_WAIT_READY_TIMEOUT(I2C0);
							if(I2C0->I2CSTATUS == 0x28){
									/* NACK */
									/* Send stop */ 
									_I2C_STOP(I2C0);
							}else{
									/* ACK */
									/* read data error */
									i32Err = 3;
							}
					}else{
							/* NACK */						
							/* Send low address error */
							i32Err = 2;
					}            
			}else{
					/* NACK */            
					/* Send control write error */
					i32Err = 1;
			}

			if(i32Err){
					/* Send stop */
					_I2C_STOP(I2C0);
					printf("err %d\t", i32Err);
					SYS_SysTickDelay(100);
					//printf("err\n");
			}
	} while(i32Err && ++try_count < 2); 
}	

uint8_t i2cRead(uint32_t u32Addr) {
	uint32_t try_count = 0;
	int32_t i32Err;
	uint8_t u8Data = 0; 
	
	do {
			i32Err = 0;
        /* Send start */
        _I2C_START(I2C0);
        _I2C_WAIT_READY_TIMEOUT(I2C0);
        /* Send control byte*/
        _I2C_WRITE(I2C0, MMA_WRITE_CONTROL); //C: [EEPROM_WRITE_ADDR = A0H = 10100000B] => [slave addr = 1010000B] + [r/w = 0] => [control = write]
        _I2C_WAIT_READY_TIMEOUT(I2C0);
        if(I2C0->I2CSTATUS == 0x18){ 
            /* ACK */
            _I2C_WRITE(I2C0, u32Addr & 0xFFUL); 			
            _I2C_WAIT_READY_TIMEOUT(I2C0);
						//printf("I2C0->I2CSTATUS = %x\n", I2C0->I2CSTATUS);
						if(I2C0->I2CSTATUS == 0x28){
								/* ACK */
								/* Send data */
								_I2C_START(I2C0); // repeat start
								_I2C_WAIT_READY_TIMEOUT(I2C0);
								//printf("I2C0->I2CSTATUS = %x\n", I2C0->I2CSTATUS);
								if(I2C0->I2CSTATUS == 0x10 || I2C0->I2CSTATUS == 0x08){
										/* ACK */
										/* Send control byte */
										_I2C_WRITE(I2C0, MMA_READ_CONTROL);
										_I2C_WAIT_READY_TIMEOUT(I2C0);												
										if(I2C0->I2CSTATUS == 0x40){
												/* Read data */
												u8Data = _I2C_READ_NAK(I2C0);
												//printf("I2C0->I2CSTATUS = %x\n", I2C0->I2CSTATUS);
												if(I2C0->I2CSTATUS == 0x58){
														/* NACK */
														/* Send stop */ 
														_I2C_STOP(I2C0);
												}else{
														/* ACK */
														/* read data error */
														i32Err = 5;
												}
										}else{
												/* NACK */
												/* Send control read error */
												i32Err = 4;
										}
								}else{
										/* NACK */
										/* Send start error */
										i32Err = 3;
								}
						}else{
								/* NACK */						
								/* Send low address error */
								i32Err = 2;
						}            
        }else{
            /* NACK */            
            /* Send control write error */
            i32Err = 1;
        }

        if(i32Err){
            /* Send stop */
            _I2C_STOP(I2C0);
						printf("err %d\t", i32Err);
            SYS_SysTickDelay(100);
						//printf("err\n");
        }
	} while(i32Err && ++try_count < 2); 
	
	return (i32Err == 0) ? u8Data : 0xFF; 
}	


void SYS_Init(void)
{

    /* Unlock protected registers */
    SYS_UnlockReg();

/*---------------------------------------------------------------------------------------------------------*/
/* Init System Clock                                                                                       */
/*---------------------------------------------------------------------------------------------------------*/

    /* Enable External XTAL (4~24 MHz) */
    SYSCLK->PWRCON |= SYSCLK_PWRCON_XTL12M_EN_Msk | SYSCLK_PWRCON_IRC10K_EN_Msk;

    /* Waiting for 12MHz & IRC10Khz clock ready */
    SYS_WaitingForClockReady( SYSCLK_CLKSTATUS_XTL12M_STB_Msk | SYSCLK_CLKSTATUS_IRC10K_STB_Msk);

    /* Switch HCLK clock source to XTAL */
    SYSCLK->CLKSEL0 = SYSCLK_CLKSEL0_HCLK_XTAL;

    /* Set PLL to power down mode and PLL_STB bit in CLKSTATUS register will be cleared by hardware.*/
    SYSCLK->PLLCON|= SYSCLK_PLLCON_PD_Msk;

    /* Set PLL frequency */        
    SYSCLK->PLLCON = PLLCON_SETTING;

    /* Waiting for clock ready */
    SYS_WaitingForClockReady(SYSCLK_CLKSTATUS_PLL_STB_Msk);

    /* Switch HCLK clock source to PLL */
    SYSCLK->CLKSEL0 = SYSCLK_CLKSEL0_HCLK_PLL;

    /* Enable IP clock */        
    SYSCLK->APBCLK = SYSCLK_APBCLK_UART0_EN_Msk | SYSCLK_APBCLK_SPI0_EN_Msk |
                        SYSCLK_APBCLK_I2C_EN_Msk;
    
    /* Select IP clock source */
    SYSCLK->CLKSEL1 = SYSCLK_CLKSEL1_UART_XTAL;

    /* Update System Core Clock */
    /* User can use SystemCoreClockUpdate() to calculate PllClock, SystemCoreClock and CycylesPerUs automatically. */
    //SystemCoreClockUpdate(); 
    PllClock        = PLL_CLOCK;            // PLL
    SystemCoreClock = PLL_CLOCK / 1;        // HCLK
    CyclesPerUs     = PLL_CLOCK / 1000000;  // For SYS_SysTickDelay()

/*---------------------------------------------------------------------------------------------------------*/
/* Init I/O Multi-function                                                                                 */
/*---------------------------------------------------------------------------------------------------------*/
    /* Set P3 multi-function pins for UART0 RXD and TXD  */
    SYS->P3_MFP = SYS_MFP_P30_RXD0 | SYS_MFP_P31_TXD0
    /* Set P3.4 P3.5 for I2C */
				 | SYS_MFP_P34_SDA0 | SYS_MFP_P35_SCL0;
    /* Set P1.4, P1.5, P1.6, P1.7 for SPI0 to driver LCD */
    SYS->P1_MFP = SYS_MFP_P14_SPISS0 | SYS_MFP_P15_MOSI_0 | SYS_MFP_P16_MISO_0 | SYS_MFP_P17_SPICLK0;

    /* Lock protected registers */
    SYS_LockReg();
}


void UART0_Init(void)
{
/*---------------------------------------------------------------------------------------------------------*/
/* Init UART                     //C: 模式2、波特率=9600、字长=8位、奇偶校验=none、1位停止位                                                                        */
/*---------------------------------------------------------------------------------------------------------*/
    UART0->BAUD = UART_BAUD_MODE2 | UART_BAUD_DIV_MODE2(__XTAL, 9600);
    _UART_SET_DATA_FORMAT(UART0, UART_WORD_LEN_8 | UART_PARITY_NONE | UART_STOP_BIT_1);
}


int main(void)
{
	int i = 0;
	int no = 1;
	
	unsigned char xout; 
	unsigned char yout; 
	unsigned char zout; 
	unsigned char lastx = 0; 
	unsigned char lasty = 0; 
	unsigned char lastz = 0;
	unsigned int delta; 

	/* Init system, IP clock and multi-function I/O */
	SYS_Init();

	/* Init UART0 for printf */	
	UART0_Init();  //C: 模式2、波特率=9600、字长=8位、奇偶校验=none、一位停止位

	_GPIO_SET_PIN_MODE(P3, 2, GPIO_PMD_OUTPUT); //C: P3.2 => mode=OUTPUT
	P3->DOUT |= (1<<2); //C: output "1" to P3.2  => 接sensor的VCC，给sensor供电
	_GPIO_SET_PIN_MODE(P3, 3, GPIO_PMD_OUTPUT); //C: P3.3 => mode=OUTPUT
	P3->DOUT |= (1<<3); //C: output "1" to P3.3  => 接sensor的sa，给其输出高电平，选择sensor的地址
	
	printf("Start Test MMA8451\n");
	printf("Please connect MMA8451 to I2C\n");	

	while(1) {
		
		I2C_Init(); 
		
		i2cWrite(0x0B, 0x00); 
		i2cWrite(0x2A, 0x01); 
		i2cWrite(0x2B, 0x02); 
		
		xout = i2cRead(0x01); 
		yout = i2cRead(0x03); 
		zout = i2cRead(0x05); 

		delta = (xout - lastx) * (xout - lastx) + (yout - lasty) * (yout - lasty) + (zout - lastz) * (zout - lastz);

		if (delta > 0xC00) {
			printf("<%04d>", i++); 
		} else {
			printf("<%04d>", i); 
		} 
		
		lastx = xout; 
		lasty = yout; 
		lastz = zout; 
		
		no++;
		Delay(3000000);
	}
    
	//return 0;
}


