import {createClient} from '@supabase/supabase-js';
import pg from 'pg';

const SUPABASE_URL = process.env.SUPABASE_URL;
const SUPABASE_SECRET_KEY = process.env.SUPABASE_SECRET_KEY;

if (!SUPABASE_URL || !SUPABASE_SECRET_KEY) {
  console.error('Faltan SUPABASE_UR o DATABASE_URL en el entorno.');
  process.exit(1);
}

const supabaseAdmin = createClient(SUPABASE_URL, SUPABASE_SECRET_KEY, {
  auth: {autoRefreshToken: false, persistSession: false}
});

const client = new pg.Client({
  host: "aws-1-eu-central-1.pooler.supabase.com",
  user: process.env.DB_USERNAME,
  database: process.env.DB_NAME,
  password: process.env.DB_PASSWORD,
  port: 5432,
  ssl: false
})

client.connect()

async function inviteManager({email, gymId, name, firstSurname, secondSurname}) {
  const {data, error} = await supabaseAdmin.auth.admin.inviteUserByEmail(email, {
    data: {
      name,
      first_surname: firstSurname,
      second_surname: secondSurname,
      gym_id: gymId
    }
  });

  if (error) {
    console.error('Error al invitar:', error);
    process.exit(1);
  }

  const userId = data.user.id;
  console.log(`Invitación enviada a ${email}. user_id: ${userId}`);

  try {
    const result = await client.query(
      `UPDATE public.users
       SET role = 'MANAGER'
       WHERE id = $1 AND gym_id = $2`,
      [userId, userId]
    );

    if (result.rowCount === 0) {
      throw new Error(`No se encontró fila en public.users con id ${userId}`);
    }

    console.log(`Usuario ${email} actualizado correctamente como ${role} del gym ${gymId}.`);
  } catch (dbError) {
    console.error('Error al actualizar public.users:', dbError.message);
    process.exit(1);
  } finally {
    await client.end()
  }
}

// --- Uso: node invite-manager.js email@ejemplo.com 3 Juan Pérez García MANAGER
const [, , email, gymId, name, firstSurname, secondSurname] = process.argv;

if (!email || !gymId) {
  console.error('Uso: node invite-manager.js <email> <gym_id> <name> <first_surname> <second_surname>');
  process.exit(1);
}

inviteManager({email, gymId: Number(gymId), name, firstSurname, secondSurname});
