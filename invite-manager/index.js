import {createClient} from '@supabase/supabase-js';
import pg from 'pg';

const SUPABASE_URL = process.env.SUPABASE_URL;
const SUPABASE_SECRET_KEY = process.env.SUPABASE_SECRET_KEY;

if (!SUPABASE_URL || !SUPABASE_SECRET_KEY) {
  console.error('Missing SUPABASE_URL or SUPABASE_SECRET_KEY in environment.');
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

async function inviteManager({email, gymId, name, firstSurname, secondSurname}) {
  console.log({email, gymId, name, firstSurname, secondSurname})
  const {data, error} = await supabaseAdmin.auth.admin.inviteUserByEmail(email, {
    data: {
      name,
      first_surname: firstSurname,
      second_surname: secondSurname,
      gym_id: gymId
    },
    redirectTo: 'https://app.ctrlgym.es/signup'
  });

  if (error) {
    console.error('Error inviting user:', error);
    process.exit(1);
  }

  const userId = data.user.id;
  console.log(`Invitation sent to ${email}. user_id: ${userId}`);

  try {
    await client.connect();
    const result = await client.query(
      `UPDATE public.users
       SET role = 'MANAGER', status = NULL
       WHERE id = $1 AND gym_id = $2`,
      [userId, gymId]
    );

    if (result.rowCount === 0) {
      throw new Error(`No row found in public.users with id ${userId}`);
    }

    console.log(`User ${email} updated successfully as MANAGER of gym ${gymId}.`);
  } catch (dbError) {
    console.error('Error updating public.users:', dbError.message);
    process.exit(1);
  } finally {
    await client.end();
  }
}

// --- Uso: node invite-manager.js email@ejemplo.com 3 Juan Pérez García
const [, , email, gymId, name, firstSurname, secondSurname] = process.argv;

if (!email || !gymId) {
  console.error('Usage: node invite-manager.js <email> <gym_id> <name> <first_surname> <second_surname>');
  process.exit(1);
}

inviteManager({email, gymId: Number(gymId), name, firstSurname, secondSurname});
