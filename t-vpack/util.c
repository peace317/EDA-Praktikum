#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include "util.h"

/* This file contains utility functions widely used in *
 * my programs.  Many are simply versions of file and  *
 * memory grabbing routines that take the same         *
 * arguments as the standard library ones, but exit    *
 * the program if they find an error condition.        */


int linenum;  /* Line in file being parsed. */


FILE *my_fopen (char *fname, char *flag, int prompt) {

 FILE *fp;   /* prompt = 1: prompt user.  prompt=0: use fname */

 while (1) {
    if (prompt) 
       scanf("%s",fname);
    if ((fp = fopen(fname,flag)) != NULL)
       break; 
    printf("Error opening file %s for %s access.\n",fname,flag);
    if (!prompt) 
       exit(1);
    printf("Please enter another filename.\n");
 }
 return (fp);
}


int my_atoi (const char *str) {

/* Returns the integer represented by the first part of the character       *
 * string.  Unlike the normal atoi, I return -1 if the string doesn't       *
 * start with a numeric digit.                                              */

 if (str[0] < '0' || str[0] > '9') 
    return (-1);

 return (atoi(str));
}


void *my_calloc (size_t nelem, size_t size) {

 void *ret;

 if ((ret = calloc (nelem,size)) == NULL) {
    fprintf(stderr,"Error:  Unable to calloc memory.  Aborting.\n");
    exit (1);
 }
 return (ret);
}


void *my_malloc (size_t size) {

 void *ret;

 if ((ret = malloc (size)) == NULL) {
    fprintf(stderr,"Error:  Unable to malloc memory.  Aborting.\n");
    abort ();
    exit (1);
 }
 return (ret);
}



void *my_realloc (void *ptr, size_t size) {

 void *ret;

 if ((ret = realloc (ptr,size)) == NULL) {
    fprintf(stderr,"Error:  Unable to realloc memory.  Aborting.\n");
    exit (1);
 }
 return (ret);
}


void *my_chunk_malloc (size_t size, struct s_linked_vptr **chunk_ptr_head, 
       int *mem_avail_ptr, char **next_mem_loc_ptr) {
 
/* This routine should be used for allocating fairly small data             *
 * structures where memory-efficiency is crucial.  This routine allocates   *
 * large "chunks" of data, and parcels them out as requested.  Whenever     *
 * it mallocs a new chunk it adds it to the linked list pointed to by       *
 * chunk_ptr_head.  This list can be used to free the chunked memory.       *
 * If chunk_ptr_head is NULL, no list of chunked memory blocks will be kept *
 * -- this is useful for data structures that you never intend to free as   *
 * it means you don't have to keep track of the linked lists.               *
 * Information about the currently open "chunk" must be stored by the       *
 * user program.  mem_avail_ptr points to an int storing how many bytes are *
 * left in the current chunk, while next_mem_loc_ptr is the address of a    *
 * pointer to the next free bytes in the chunk.  To start a new chunk,      *
 * simply set *mem_avail_ptr = 0.  Each independent set of data structures  *
 * should use a new chunk.                                                  */
 
/* To make sure the memory passed back is properly aligned, I must *
 * only send back chunks in multiples of the worst-case alignment  *
 * restriction of the machine.  On most machines this should be    *
 * a long, but on 64-bit machines it might be a long long or a     *
 * double.  Change the typedef below if this is the case.          */

 typedef long Align;

#define CHUNK_SIZE 32768
#define FRAGMENT_THRESHOLD 100

 char *tmp_ptr;
 int aligned_size;

 if (*mem_avail_ptr < size) {       /* Need to malloc more memory. */
    if (size > CHUNK_SIZE) {      /* Too big, use standard routine. */
       tmp_ptr = my_malloc (size);

/*#ifdef DEBUG
       printf("NB:  my_chunk_malloc got a request for %d bytes.\n",
          size);
       printf("You should consider using my_malloc for such big requests.\n");
#endif */

       if (chunk_ptr_head != NULL) 
          *chunk_ptr_head = insert_in_vptr_list (*chunk_ptr_head, tmp_ptr);
       return (tmp_ptr);
    }

    if (*mem_avail_ptr < FRAGMENT_THRESHOLD) {  /* Only a small scrap left. */
       *next_mem_loc_ptr = my_malloc (CHUNK_SIZE);
       *mem_avail_ptr = CHUNK_SIZE;
       if (chunk_ptr_head != NULL) 
          *chunk_ptr_head = insert_in_vptr_list (*chunk_ptr_head, 
                            *next_mem_loc_ptr);
    }

/* Execute else clause only when the chunk we want is pretty big,  *
 * and would leave too big an unused fragment.  Then we use malloc *
 * to allocate normally.                                           */

    else {     
       tmp_ptr = my_malloc (size);
       if (chunk_ptr_head != NULL) 
          *chunk_ptr_head = insert_in_vptr_list (*chunk_ptr_head, tmp_ptr);
       return (tmp_ptr);
    }
 }

/* Find the smallest distance to advance the memory pointer and keep *
 * everything aligned.                                               */

 if (size % sizeof (Align) == 0) {
    aligned_size = size;
 }
 else {
    aligned_size = size + sizeof(Align) - size % sizeof(Align);
 }

 tmp_ptr = *next_mem_loc_ptr;
 *next_mem_loc_ptr += aligned_size; 
 *mem_avail_ptr -= aligned_size;
 return (tmp_ptr);
}


void free_chunk_memory (struct s_linked_vptr *chunk_ptr_head) {

/* Frees the memory allocated by a sequence of calls to my_chunk_malloc. */

 struct s_linked_vptr *curr_ptr, *prev_ptr;

 curr_ptr = chunk_ptr_head;
 
 while (curr_ptr != NULL) {
    free (curr_ptr->data_vptr);   /* Free memory "chunk". */
    prev_ptr = curr_ptr;
    curr_ptr = curr_ptr->next;
    free (prev_ptr);              /* Free memory used to track "chunk". */
 }
}


struct s_linked_vptr *insert_in_vptr_list (struct s_linked_vptr *head,
              void *vptr_to_add) {

/* Inserts a new element at the head of a linked list of void pointers. *
 * Returns the new head of the list.                                    */

 struct s_linked_vptr *linked_vptr;

 linked_vptr = (struct s_linked_vptr *) my_malloc (sizeof(struct 
                 s_linked_vptr));

 linked_vptr->data_vptr = vptr_to_add;
 linked_vptr->next = head;
 return (linked_vptr);     /* New head of the list */
}


t_linked_int *insert_in_int_list (t_linked_int *head, int data, t_linked_int **
      free_list_head_ptr) {

/* Inserts a new element at the head of a linked list of integers.  Returns  *
 * the new head of the list.  One argument is the address of the head of     *
 * a list of free ilist elements.  If there are any elements on this free    *
 * list, the new element is taken from it.  Otherwise a new one is malloced. */

 t_linked_int *linked_int;

 if (*free_list_head_ptr != NULL) {
    linked_int = *free_list_head_ptr;
    *free_list_head_ptr = linked_int->next;
 }
 else {
    linked_int = (t_linked_int *) my_malloc (sizeof (t_linked_int));
 }
  
 linked_int->data = data;
 linked_int->next = head;
 return (linked_int);
}


void free_int_list (t_linked_int **int_list_head_ptr) { 
 
/* This routine truly frees (calls free) all the integer list elements    * 
 * on the linked list pointed to by *head, and sets head = NULL.          */ 
 
 t_linked_int *linked_int, *next_linked_int;
 
 linked_int = *int_list_head_ptr; 
   
 while (linked_int != NULL) { 
    next_linked_int = linked_int->next;
    free (linked_int);
    linked_int = next_linked_int; 
 } 
 
 *int_list_head_ptr = NULL; 
} 


void alloc_ivector_and_copy_int_list (t_linked_int **list_head_ptr,
            int num_items, struct s_ivec *ivec, t_linked_int
            **free_list_head_ptr) {

/* Allocates an integer vector with num_items elements and copies the       *
 * integers from the list pointed to by list_head (of which there must be   *
 * num_items) over to it.  The int_list is then put on the free list, and   *
 * the list_head_ptr is set to NULL.                                        */

 t_linked_int *linked_int, *list_head;
 int i, *list;

 list_head = *list_head_ptr;

 if (num_items == 0) {    /* Empty list. */
    ivec->nelem = 0;
    ivec->list = NULL;
    
    if (list_head != NULL) {
       printf ("Error in alloc_ivector_and_copy_int_list:\n Copied %d "
           "elements, but list at %p contains more.\n", num_items, list_head);
       exit (1);
    }
    return;
 }
 
 ivec->nelem = num_items;
 list = (int *) my_malloc (num_items * sizeof (int));
 ivec->list = list;
 linked_int = list_head;
 
 for (i=0;i<num_items-1;i++) {
    list[i] = linked_int->data;
    linked_int = linked_int->next;
 }
 
 list[num_items-1] = linked_int->data;
 
 if (linked_int->next != NULL) {
    printf ("Error in alloc_ivector_and_copy_int_list:\n Copied %d elements, "
            "but list at %p contains more.\n", num_items, list_head);
    exit (1);
 }
 
 linked_int->next = *free_list_head_ptr;
 *free_list_head_ptr = list_head;
 *list_head_ptr = NULL;
}


static int cont;  /* line continued? */

char *my_fgets(char *buf, int max_size, FILE *fp) {
 /* Get an input line, update the line number and cut off *
  * any comment part.  A \ at the end of a line with no   *
  * comment part (#) means continue.                      */

 char *val;
 int i;
 
 cont = 0;
 val = fgets(buf,max_size,fp);
 linenum++;
 if (val == NULL) return(val);

/* Check that line completely fit into buffer.  (Flags long line   *
 * truncation).                                                    */

 for (i=0;i<max_size;i++) {
    if (buf[i] == '\n') 
       break;
    if (buf[i] == '\0') {
       printf("Error on line %d -- line is too long for input buffer.\n",
          linenum);
       printf("All lines must be at most %d characters long.\n",BUFSIZE-2);
       printf("The problem could also be caused by a missing newline.\n");
       exit (1);
    }
 }


 for (i=0;i<max_size && buf[i] != '\0';i++) {
    if (buf[i] == '#') {
        buf[i] = '\0';
        break;
    }
 }

 if (i<2) return (val);
 if (buf[i-1] == '\n' && buf[i-2] == '\\') { 
    cont = 1;   /* line continued */
    buf[i-2] = '\n';  /* May need this for tokens */
    buf[i-1] = '\0';
 }
 return(val);
}


char *my_strtok(char *ptr, char *tokens, FILE *fp, char *buf) {

/* Get next token, and wrap to next line if \ at end of line.    *
 * There is a bit of a "gotcha" in strtok.  It does not make a   *
 * copy of the character array which you pass by pointer on the  *
 * first call.  Thus, you must make sure this array exists for   *
 * as long as you are using strtok to parse that line.  Don't    *
 * use local buffers in a bunch of subroutines calling each      *
 * other; the local buffer may be overwritten when the stack is  *
 * restored after return from the subroutine.                    */

 char *val;

 val = strtok(ptr,tokens);
 while (1) {
    if (val != NULL || cont == 0) return(val);
   /* return unless we have a null value and a continuation line */
    if (my_fgets(buf,BUFSIZE,fp) == NULL) 
       return(NULL);
    val = strtok(buf,tokens);
 }
}


void free_ivec_vector (struct s_ivec *ivec_vector, int nrmin, int nrmax) {

/* Frees a 1D array of integer vectors.                              */

 int i;  

 for (i=nrmin;i<=nrmax;i++)
    if (ivec_vector[i].nelem != 0)
       free (ivec_vector[i].list);

 free (ivec_vector + nrmin);
}


void free_ivec_matrix (struct s_ivec **ivec_matrix, int nrmin, int nrmax, 
       int ncmin, int ncmax) {

/* Frees a 2D matrix of integer vectors (ivecs).                     */  

 int i, j;

 for (i=nrmin;i<=nrmax;i++) {
    for (j=ncmin;j<=ncmax;j++) {
       if (ivec_matrix[i][j].nelem != 0) {
          free (ivec_matrix[i][j].list);
       } 
    }
 }     
 
 free_matrix (ivec_matrix, nrmin, nrmax, ncmin, sizeof (struct s_ivec));
}


void free_ivec_matrix3 (struct s_ivec ***ivec_matrix3, int nrmin, int nrmax,
       int ncmin, int ncmax, int ndmin, int ndmax) {

/* Frees a 3D matrix of integer vectors (ivecs).                     */  

 int i, j, k;

 for (i=nrmin;i<=nrmax;i++) {
    for (j=ncmin;j<=ncmax;j++) {
       for (k=ndmin;k<=ndmax;k++) {
          if (ivec_matrix3[i][j][k].nelem != 0) {
             free (ivec_matrix3[i][j][k].list);
          }
       }
    }
 }
 
 free_matrix3 (ivec_matrix3, nrmin, nrmax, ncmin, ncmax, ndmin, 
               sizeof (struct s_ivec));
}


void **alloc_matrix (int nrmin, int nrmax, int ncmin, int ncmax, 
   size_t elsize) {

/* allocates an generic matrix with nrmax-nrmin + 1 rows and ncmax - *
 * ncmin + 1 columns, with each element of size elsize. i.e.         *
 * returns a pointer to a storage block [nrmin..nrmax][ncmin..ncmax].*
 * Simply cast the returned array pointer to the proper type.        */

 int i;
 char **cptr;

 cptr = (char **) my_malloc ((nrmax - nrmin + 1) * sizeof (char *));
 cptr -= nrmin;
 for (i=nrmin;i<=nrmax;i++) {
    cptr[i] = (char *) my_malloc ((ncmax - ncmin + 1) * elsize);
    cptr[i] -= ncmin * elsize / sizeof(char);  /* sizeof(char) = 1 */
 }   
 return ((void **) cptr);
}


/* NB:  need to make the pointer type void * instead of void ** to allow   *
 * any pointer to be passed in without a cast.                             */

void free_matrix (void *vptr, int nrmin, int nrmax, int ncmin,
                  size_t elsize) {

 int i;
 char **cptr;

 cptr = (char **) vptr;

 for (i=nrmin;i<=nrmax;i++) 
    free (cptr[i] + ncmin * elsize / sizeof (char));
 free (cptr + nrmin);   
} 


void ***alloc_matrix3 (int nrmin, int nrmax, int ncmin, int ncmax, 
      int ndmin, int ndmax, size_t elsize) {

/* allocates a 3D generic matrix with nrmax-nrmin + 1 rows, ncmax -  *
 * ncmin + 1 columns, and a depth of ndmax-ndmin + 1, with each      *
 * element of size elsize. i.e. returns a pointer to a storage block *
 * [nrmin..nrmax][ncmin..ncmax][ndmin..ndmax].  Simply cast the      *
 *  returned array pointer to the proper type.                       */

 int i, j;
 char ***cptr;

 cptr = (char ***) my_malloc ((nrmax - nrmin + 1) * sizeof (char **));
 cptr -= nrmin;
 for (i=nrmin;i<=nrmax;i++) {
    cptr[i] = (char **) my_malloc ((ncmax - ncmin + 1) * sizeof (char *));
    cptr[i] -= ncmin;
    for (j=ncmin;j<=ncmax;j++) {
       cptr[i][j] = (char *) my_malloc ((ndmax - ndmin + 1) * elsize);
       cptr[i][j] -= ndmin * elsize / sizeof(char); /* sizeof(char) = 1) */
    }
 }   
 return ((void ***) cptr);
}


void free_matrix3 (void *vptr, int nrmin, int nrmax, int ncmin, int ncmax, 
        int ndmin, size_t elsize) {

 int i, j;
 char ***cptr;

 cptr = (char ***) vptr;

 for (i=nrmin;i<=nrmax;i++) {
    for (j=ncmin;j<=ncmax;j++) 
       free (cptr[i][j] + ndmin * elsize / sizeof (char));
    free (cptr[i] + ncmin);
 }
 free (cptr + nrmin);   
} 


/* Portable random number generator defined below.  Taken from ANSI C by  *
 * K & R.  Not a great generator, but fast, and good enough for my needs. */

#define IA 1103515245u
#define IC 12345u
#define IM 2147483648u
#define CHECK_RAND 

static unsigned int current_random = 0;


void my_srandom (int seed) {

 current_random = (unsigned int) seed;
}


int my_irand (int imax) {

/* Creates a random integer between 0 and imax, inclusive.  i.e. [0..imax] */ 

 int ival;

/* current_random = (current_random * IA + IC) % IM; */
 current_random = current_random * IA + IC;  /* Use overflow to wrap */
 ival = current_random & (IM - 1);  /* Modulus */
 ival = (int) ((float) ival * (float) (imax + 0.999) / (float) IM);

#ifdef CHECK_RAND
 if ((ival < 0) || (ival > imax)) {
    printf("Bad value in my_irand, imax = %d  ival = %d\n",imax,ival);
    exit(1);
 }
#endif

 return(ival);
}
 

float my_frand (void) {
 
/* Creates a random float between 0 and 1.  i.e. [0..1).        */ 
 
 float fval;
 int ival;

 current_random = current_random * IA + IC;  /* Use overflow to wrap */
 ival = current_random & (IM - 1);  /* Modulus */
 fval = (float) ival / (float) IM;

#ifdef CHECK_RAND
 if ((fval < 0) || (fval > 1.)) {
    printf("Bad value in my_frand, fval = %g\n",fval);
    exit(1);
 }
#endif
 
 return(fval);
}

