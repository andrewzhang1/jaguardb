INCDIR=$(HOME)/jaguar/include
STATLINK=-static-libgcc -static-libstdc++ -static
STATLINK= 

.cc.o:
	 g++ -I$(INCDIR) $(STATLINK) -c -o $@ $<

jaguarimport: main.o  postgreimport.o
	g++ -o $@ -I$(INCDIR) $(STATLINK) \
	main.o  postgreimport.o \
	-L $(HOME)/jaguar/lib -L/usr/lib64 -I/usr/include/pgsql/ -lpq -ljaguar -lstdc++ -g


clean:
	/bin/rm -f *.o
