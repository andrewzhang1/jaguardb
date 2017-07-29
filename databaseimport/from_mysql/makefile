INCDIR=$(HOME)/jaguar/include
STATLINK=-static-libgcc -static-libstdc++ -static
STATLINK= 

.cc.o:
	 g++ -I$(INCDIR) `mysql_config --cflags --libs` $(STATLINK) -c -o $@ $<

jaguarimport: main.o mysqlimport.o 
	g++ -o $@ -I$(INCDIR) $(STATLINK) \
	`mysql_config --cflags --libs` \
	main.o mysqlimport.o  \
	-L $(HOME)/jaguar/lib -L/usr/lib64 -lpq -ljaguar -lstdc++ -g


clean:
	/bin/rm -f *.o
