## Tested single Instance

### Case 1: set MainActivity as single Instance

  Input: MainActivity-> A->B->C->D-> MainActivity
  
  Pressed Again MainActivity-> launced D->MainActivity->D
  Pressed Again MainActivity-> launced D->MainActivity->D
  Pressed Again MainActivity -> launced D->MainActivity->D
  Pressed Again MainActivity-> launced D->MainActivity->D
  ##### Note: D is full stack backed with ABC
  BackPressed: A->B->C->D
  BackPressed: 
        1 press A->B->C->D
        2 press A->B->C
        3 press A->B
        4 press A
        5 press close, No MainActivity
        
### Case 2: set Activity A as single Top

  Input: MainActivity-> A->B->C->D-> MainActivity->A->B->C->D-> MainActivity->A->B->C->D-> MainActivity->A->B->C->D-> MainActivity
  Backpressed also same in sequence exactly like standard launch mode
  
  Input: MainActivity-> A->A(no new instance, old instance but with onNewIntent called)
  
### Case 3: set Activity A as single Task

  Input: MainActivity-> A->B->C->D-> MainActivity->A->B->C->D-> MainActivity->A
  Backpressed only MainActivity->A, all transition destroyed whenever called A next time.
  But when A called next time,no new instance, old instance with onNewIntent called
  
  ##### Note: but when we launching Activity A using startActivityForResult then its always create new instance
  
  
  
