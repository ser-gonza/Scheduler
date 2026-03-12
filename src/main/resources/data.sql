merge into major (id, name, total_credits_required, arts_humanities_required, math_science_required, major_credits_required) key(id) values
    (1, 'Computer Science', 120, 12, 20, 45),
    (2, 'Informatics', 120, 12, 18, 42);

merge into course (id, code, title, credits, timeslot, credit_category, requirement_type, prerequisites) key(id) values
    (1, 'ENG-W131', 'Elementary Composition', 3, 'MWF 09:00-09:50', 'Arts & Humanities', 'GenEd', 'None'),
    (2, 'MATH-M118', 'Finite Mathematics', 3, 'TR 10:15-11:30', 'Math & Science', 'GenEd', 'None'),
    (3, 'MATH-M119', 'Brief Survey of Calculus I', 3, 'MWF 11:15-12:05', 'Math & Science', 'GenEd', 'None'),
    (4, 'MATH-M212', 'Calculus II', 4, 'TR 09:30-10:45', 'Math & Science', 'GenEd', 'MATH-M119'),
    (5, 'CSCI-C211', 'Introduction to Computer Science', 4, 'TR 01:00-02:15', 'Major', 'Core', 'None'),
    (6, 'CSCI-C212', 'Software Systems', 4, 'MWF 10:10-11:00', 'Major', 'Core', 'CSCI-C211'),
    (7, 'CSCI-C241', 'Discrete Structures for Computer Science', 3, 'TR 12:45-02:00', 'Major', 'Core', 'CSCI-C211'),
    (8, 'CSCI-C343', 'Data Structures', 4, 'TR 11:15-12:30', 'Major', 'Core', 'CSCI-C212'),
    (9, 'CSCI-C291', 'System Programming with C and Unix', 3, 'MWF 02:30-03:20', 'Major', 'Core', 'CSCI-C212'),
    (10, 'CSCI-C323', 'Mobile App Development', 3, 'MWF 04:00-05:15', 'Major', 'Elective', 'CSCI-C212'),
    (11, 'CSCI-B365', 'Introduction to Data Analysis and Mining', 3, 'TR 03:00-04:15', 'Major', 'Elective', 'CSCI-C343'),
    (12, 'INFO-I101', 'Introduction to Informatics', 4, 'TR 08:00-09:15', 'Major', 'Core', 'None'),
    (13, 'INFO-I201', 'Mathematical Foundations of Informatics', 3, 'MWF 01:25-02:15', 'Major', 'Core', 'INFO-I101'),
    (14, 'INFO-I210', 'Information Infrastructure I', 3, 'TR 02:30-03:45', 'Major', 'Core', 'INFO-I101'),
    (15, 'INFO-I211', 'Information Infrastructure II', 3, 'MWF 12:20-01:10', 'Major', 'Core', 'INFO-I210'),
    (16, 'HIST-H105', 'American History I', 3, 'MWF 01:25-02:15', 'Arts & Humanities', 'GenEd', 'None'),
    (17, 'PHIL-P140', 'Introduction to Ethics', 3, 'TR 02:30-03:45', 'Arts & Humanities', 'GenEd', 'None'),
    (18, 'ENG-L202', 'Literary Interpretation', 3, 'MWF 11:15-12:05', 'Arts & Humanities', 'GenEd', 'ENG-W131'),
    (19, 'SPAN-S100', 'Elementary Spanish I', 4, 'TR 11:15-12:30', 'Arts & Humanities', 'GenEd', 'None'),
    (20, 'CHEM-C101', 'Elementary Chemistry I', 3, 'MWF 09:05-09:55', 'Math & Science', 'GenEd', 'None'),
    (21, 'BIOL-L100', 'Humans and the Biological World', 3, 'TR 01:00-02:15', 'Math & Science', 'GenEd', 'None'),
    (22, 'STAT-S301', 'Applied Statistical Methods', 3, 'MWF 03:35-04:25', 'Math & Science', 'GenEd', 'MATH-M118'),
    (23, 'BUS-K201', 'The Computer in Business', 3, 'TR 09:30-10:45', 'Major', 'Elective', 'None'),
    (24, 'ECON-E201', 'Introduction to Microeconomics', 3, 'MWF 10:10-11:00', 'Arts & Humanities', 'GenEd', 'None');