INSERT INTO "public"."gyms" ("id", "name", "verifacti_api_key", "stripe_account_id")
VALUES (1, 'WolfGym', 'vf_test_it', 'acct_1TcURmFylbmcvw95');

INSERT INTO "public"."exercises" ("name", "description", "muscle_group", "image", "gym_id")
VALUES ('Press de banca', null, 'CHEST',
        'https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
        1),
       ('Dominadas', null, 'BACK',
        'https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
        1),
       ('Press militar', null, 'DELTOID',
        'https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
        1),
       ('Sentadilla', null, 'QUADRICEPS',
        'https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
        1),
       ('Remo con barra', null, 'BACK',
        'https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
        1),
       ('Remo con mancuerna', null, 'BACK',
        'https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
        1),
       ('Aperturas en polea', null, 'CHEST',
        'https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
        1),
       ('Press inclinado con mancuernas', null, 'CHEST',
        'https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
        1),
       ('Press en máquina', null, 'CHEST',
        'https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
        1),
       ('Gemelo sentado', null, 'CALVES',
        'https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
        1),
       ('Gemelo de pie', null, 'CALVES',
        'https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
        1),
       ('Femoral tumbado', null, 'HAMSTRINGS',
        'https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
        1),
       ('Femoral sentado', null, 'HAMSTRINGS',
        'https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
        1),
       ('Femoral de pie', null, 'HAMSTRINGS',
        'https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
        1),
       ('Sentadilla hack', null, 'QUADRICEPS',
        'https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
        1),
       ('Prensa', null, 'QUADRICEPS',
        'https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
        1),
       ('Extensiones', null, 'QUADRICEPS',
        'https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
        1),
       ('Sentadilla búlgara', null, 'QUADRICEPS',
        'https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
        1),
       ('Prensa de placas', null, 'QUADRICEPS',
        'https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
        1),
       ('Sentadilla smith', null, 'QUADRICEPS',
        'https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
        1);

INSERT INTO "public"."membership_cancellation_reasons" ("id", "code", "sort_order", "active", "created_at")
VALUES (1, 'PRICE_TOO_HIGH', 1, true, '2026-05-16 17:26:25.709569'),
       (2, 'NOT_USING_GYM', 2, true, '2026-05-16 17:26:25.709569'),
       (3, 'MOVING_CITY', 3, true, '2026-05-16 17:26:25.709569'),
       (4, 'MEDICAL_REASONS', 4, true, '2026-05-16 17:26:25.709569'),
       (5, 'SCHEDULE_INCOMPATIBILITY', 5, true, '2026-05-16 17:26:25.709569'),
       (6, 'BAD_EXPERIENCE', 6, true, '2026-05-16 17:26:25.709569'),
       (7, 'FOUND_ANOTHER_GYM', 7, true, '2026-05-16 17:26:25.709569'),
       (8, 'TEMPORARY_BREAK', 8, true, '2026-05-16 17:26:25.709569'),
       (9, 'FACILITIES_NOT_GOOD', 9, true, '2026-05-16 17:26:25.709569'),
       (10, 'CLASSES_NOT_SATISFACTORY', 10, true, '2026-05-16 17:26:25.709569'),
       (11, 'PERSONAL_REASONS', 11, true, '2026-05-16 17:26:25.709569'),
       (12, 'WORK_REASONS', 12, true, '2026-05-16 17:26:25.709569'),
       (13, 'ECONOMIC_REASONS', 13, true, '2026-05-16 17:26:25.709569'),
       (14, 'LACK_OF_RESULTS', 14, true, '2026-05-16 17:26:25.709569'),
       (15, 'TOO_CROWDED', 15, true, '2026-05-16 17:26:25.709569'),
       (16, 'OTHER', 16, true, '2026-05-16 17:26:25.709569');

INSERT INTO "public"."membership_cancellation_reason_translations" ("cancellation_reason_id", "language_code", "name", "description")
VALUES (1, 'es', 'Precio demasiado alto', 'La cuota o los servicios son demasiado caros'),
       (2, 'es', 'No utiliza el gimnasio', 'No está aprovechando la membresía'),
       (3, 'es', 'Cambio de ciudad', 'Se muda a otra ciudad o ubicación'),
       (4, 'es', 'Motivos médicos', 'Problemas de salud o lesión'),
       (5, 'es', 'Incompatibilidad horaria', 'Los horarios no encajan con su rutina'),
       (6, 'es', 'Mala experiencia', 'Problemas con el servicio o atención'),
       (7, 'es', 'Cambio a otro gimnasio', 'Ha encontrado otro gimnasio que prefiere'),
       (8, 'es', 'Descanso temporal', 'Quiere pausar la actividad física temporalmente'),
       (9, 'es', 'Instalaciones insuficientes', 'Las instalaciones no cumplen sus expectativas'),
       (10, 'es', 'Clases no satisfactorias', 'Las clases ofrecidas no le satisfacen'),
       (11, 'es', 'Motivos personales', 'Razones personales no especificadas'),
       (12, 'es', 'Motivos laborales', 'Cambios o exigencias laborales'),
       (13, 'es', 'Motivos económicos', 'Situación económica personal'),
       (14, 'es', 'Falta de resultados', 'No percibe resultados esperados'),
       (15, 'es', 'Demasiada afluencia', 'El gimnasio suele estar demasiado lleno'),
       (16, 'es', 'Otros', 'Otro motivo no especificado');

INSERT INTO "public"."expense_categories" ("id", "code", "created_at", "is_active")
VALUES (1, 'RENT', '2026-05-16 20:06:58.090593', true),
       (2, 'UTILITIES', '2026-05-16 20:06:58.090593', true),
       (3, 'CLEANING', '2026-05-16 20:06:58.090593', true),
       (4, 'MAINTENANCE', '2026-05-16 20:06:58.090593', true),
       (5, 'EQUIPMENT', '2026-05-16 20:06:58.090593', true),
       (6, 'MARKETING', '2026-05-16 20:06:58.090593', true),
       (7, 'SOFTWARE', '2026-05-16 20:06:58.090593', true),
       (8, 'OTHER', '2026-05-16 20:06:58.090593', true);

INSERT INTO "public"."expense_category_translations" ("expense_category_id", "language_code", "name", "description")
VALUES (1, 'es', 'Alquiler del local del gimnasio', null),
       (2, 'es', 'Suministros: luz, agua, gas', null),
       (3, 'es', 'Servicios de limpieza', null),
       (4, 'es', 'Mantenimiento general y reparaciones', null),
       (5, 'es', 'Maquinaria y equipamiento deportivo', null),
       (6, 'es', 'Publicidad y marketing', null),
       (7, 'es', 'Licencias y SaaS', null),
       (8, 'es', 'Otros gastos', null);

INSERT INTO "public"."members" ("id", "gym_id", "email", "name", "first_surname", "second_surname", "gender", "birth_date", "created_at",  "stripe_payment_method_id", "status")
VALUES ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 1, 'john.doe@example.com', 'John', 'Doe', 'Smith', 'M', '1990-05-15', now(), 'pm_test456',  'AUTH');

INSERT INTO "public"."membership_plans" ("id", "gym_id", "name", "price", "billing_period", "active", "created_at",
                                         "stripe_price_id")
VALUES ('plan_basic', 1, 'Basic', 29.99, 'MONTHLY', true, '2026-01-01', 'price_basic123');

INSERT INTO "public"."membership_plans" ("id", "gym_id", "name", "price", "billing_period", "active", "created_at",
                                         "stripe_price_id")
VALUES ('plan_premium', 1, 'Premium', 49.99, 'MONTHLY', true, '2026-01-01', 'price_premium456');

INSERT INTO "public"."gym_branches" ("id", "gym_id", "name", "is_active", "capacity")
VALUES (1, 1, 'Main Branch', true, 100);

INSERT INTO "public"."membership_plan_branches" ("membership_plan_id", "branch_id")
VALUES ('plan_basic', 1),
       ('plan_premium', 1);

--INSERT INTO "public"."memberships" ("member_id", "gym_id", "membership_plan_id", "start_date", "end_date", "next_billing_date", "auto_renew")
--VALUES ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 1, 'plan_premium', '2026-01-01', null, '2026-02-01', true);
