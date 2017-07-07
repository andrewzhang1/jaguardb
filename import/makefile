INCDIR=$(HOME)/jaguar/include
STATLINK=-static-libgcc -static-libstdc++ -static
STATLINK= 

.cc.o:
	 g++ -I$(INCDIR) `mysql_config --cflags --libs` $(STATLINK) -c -o $@ $<

jaguarimport: main.o mysqlimport.o  postgreimport.o
	g++ -o $@ -I$(INCDIR) $(STATLINK) \
	`mysql_config --cflags --libs` \
	main.o mysqlimport.o  postgreimport.o \
	-L $(HOME)/jaguar/lib -L/usr/lib64 -I/usr/include/pgsql/ -lpq -ljaguar -lstdc++ -g


clean:
	/bin/rm -f *.o
