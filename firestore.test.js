const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read user profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user: can create and read their own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      username: "AliceFighter",
      level: 1,
      trophies: 100,
      coins: 50,
      createdAt: new Date(),
    })
  );

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
});

test("Cross-user access: Bob cannot read or modify Alice profile", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      username: "AliceFighter",
      level: 1,
      trophies: 100,
      coins: 50,
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      trophies: 0,
    })
  );
});

test("Leaderboard: Signed-in user can read all, but only write own record", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("leaderboard").doc(ALICE_UID).set({
      userId: ALICE_UID,
      username: "AliceFighter",
      trophies: 1500,
      wins: 20,
      level: 5,
      favoriteFish: "oranda_goldfish",
    })
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  // Bob can read leaderboard
  await assertSucceeds(bobDb.collection("leaderboard").doc(ALICE_UID).get());
  await assertSucceeds(bobDb.collection("leaderboard").get());

  // But Bob cannot overwrite Alice's score
  await assertFails(
    bobDb.collection("leaderboard").doc(ALICE_UID).set({
      userId: ALICE_UID,
      username: "Hacked",
      trophies: 0,
      wins: 0,
      level: 1,
    })
  );
});

test("Match: Alice can create match, Bob can join", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const matchId = "match_100";
  await assertSucceeds(
    aliceDb.collection("matches").doc(matchId).set({
      id: matchId,
      hostId: ALICE_UID,
      hostName: "Alice",
      hostFish: "oranda_goldfish",
      hostHp: 100,
      status: "waiting",
      createdAt: new Date(),
    })
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(
    bobDb.collection("matches").doc(matchId).update({
      guestId: BOB_UID,
      guestName: "Bob",
      guestFish: "warsee_fish",
      guestHp: 100,
      guestMaxHp: 100,
      status: "active",
    })
  );
});
